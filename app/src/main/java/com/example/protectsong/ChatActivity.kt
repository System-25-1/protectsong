package com.example.protectsong

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.adapter.ChatAdapter
import com.example.protectsong.databinding.ActivityChatBinding
import com.example.protectsong.model.ChatDisplayItem
import com.example.protectsong.model.ChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ChatAdapter
    private lateinit var messageListener: ListenerRegistration

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val adminUid = "MecPxatzCTMeHztzELY4ps4KVeh2"
    private val currentUserId: String by lazy { auth.currentUser?.uid ?: "" }
    private val targetUserId: String by lazy {
        if (currentUserId == adminUid) {
            intent.getStringExtra("chatWithUserId") ?: ""
        } else adminUid
    }
    private val chatDocumentId: String by lazy {
        if (currentUserId == adminUid) targetUserId else currentUserId
    }

    private val PICK_IMAGE = 1
    private val PICK_VIDEO = 2
    private val CAPTURE_IMAGE = 3
    private val CAPTURE_VIDEO = 4
    private lateinit var capturedUri: Uri

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val REQUEST_PERMISSIONS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS)
        }

        findViewById<TextView>(R.id.backText).setOnClickListener {
            val intent = if (currentUserId == adminUid) {
                Intent(this, ChatListActivity::class.java)
            } else {
                Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
        }

        adapter = ChatAdapter(emptyList(), currentUserId)
        binding.recyclerViewChat.adapter = adapter
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(this)

        binding.btnSend.setOnClickListener {
            val text = binding.editMessage.text.toString().trim()
            if (text.isNotEmpty()) sendTextMessage(text)
        }

        var isExpanded = false
        binding.btnToggleAttachment.setOnClickListener {
            isExpanded = !isExpanded
            binding.attachmentLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.btnToggleAttachment.setImageResource(
                if (isExpanded) R.drawable.ic_minus else R.drawable.ic_plus
            )
        }

        binding.btnImage.setOnClickListener { selectImageFromGallery() }
        binding.btnVideo.setOnClickListener { selectVideoFromGallery() }
        binding.btnCamera.setOnClickListener { showCameraDialog() }

        binding.bottomNavigation.selectedItemId = R.id.nav_chat
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_post -> {
                    startActivity(Intent(this, PostListActivity::class.java))
                    true
                }
                else -> true
            }
        }

        listenForMessages()
    }

    private fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun sendTextMessage(text: String) {
        val message = ChatMessage(
            text = text,
            senderId = currentUserId,
            receiverId = targetUserId,
            time = Timestamp.now()
        )

        db.collection("chats")
            .document(chatDocumentId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                binding.editMessage.text.clear()
                db.collection("chats")
                    .document(chatDocumentId)
                    .set(mapOf("updatedAt" to System.currentTimeMillis()), SetOptions.merge())
            }
            .addOnFailureListener {
                Toast.makeText(this, "메시지 전송 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listenForMessages() {
        messageListener = db.collection("chats")
            .document(chatDocumentId)
            .collection("messages")
            .orderBy("time")
            .addSnapshotListener { snapshots, _ ->
                val rawMessages = snapshots?.documents?.mapNotNull {
                    it.toObject(ChatMessage::class.java)
                }?.filter {
                    (it.senderId == currentUserId && it.receiverId == targetUserId) ||
                            (it.senderId == targetUserId && it.receiverId == currentUserId)
                } ?: return@addSnapshotListener

                val displayItems = mutableListOf<ChatDisplayItem>()
                var lastDate: String? = null
                val dateFormat = SimpleDateFormat("yyyy.MM.dd.E", Locale.KOREA)

                for (msg in rawMessages) {
                    val currentDate = dateFormat.format(msg.time.toDate())
                    if (currentDate != lastDate) {
                        displayItems.add(ChatDisplayItem.DateHeader(currentDate))
                        lastDate = currentDate
                    }
                    displayItems.add(ChatDisplayItem.MessageItem(msg))
                }

                adapter = ChatAdapter(displayItems, currentUserId)
                binding.recyclerViewChat.adapter = adapter
                adapter.notifyDataSetChanged()
                binding.recyclerViewChat.scrollToPosition(displayItems.size - 1)
            }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(intent, PICK_IMAGE)
    }

    private fun selectVideoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "video/*" }
        startActivityForResult(intent, PICK_VIDEO)
    }

    private fun showCameraDialog() {
        AlertDialog.Builder(this)
            .setTitle("촬영 선택")
            .setItems(arrayOf("사진 촬영", "동영상 촬영")) { _, which ->
                if (which == 0) captureImage()
                else captureVideo()
            }.show()
    }

    private fun captureImage() {
        val imageFile = File.createTempFile("img_", ".jpg", getExternalFilesDir("Pictures"))
        capturedUri = FileProvider.getUriForFile(this, "${packageName}.provider", imageFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, capturedUri)
        }
        startActivityForResult(intent, CAPTURE_IMAGE)
    }

    private fun captureVideo() {
        val videoFile = File.createTempFile("vid_", ".mp4", getExternalFilesDir("Movies"))
        capturedUri = FileProvider.getUriForFile(this, "${packageName}.provider", videoFile)
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, capturedUri)
        }
        startActivityForResult(intent, CAPTURE_VIDEO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return

        val uri = when (requestCode) {
            PICK_IMAGE, PICK_VIDEO -> data?.data
            CAPTURE_IMAGE, CAPTURE_VIDEO -> capturedUri
            else -> null
        }

        val mediaType = when (requestCode) {
            PICK_IMAGE, CAPTURE_IMAGE -> "image"
            PICK_VIDEO, CAPTURE_VIDEO -> "video"
            else -> null
        }

        if (uri != null && mediaType != null) {
            uploadMediaAndSend(uri, mediaType)
        }
    }

    private fun uploadMediaAndSend(uri: Uri, type: String) {
        val fileName = UUID.randomUUID().toString()
        val storageRef = FirebaseStorage.getInstance().getReference("chat_$type/$fileName")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val message = ChatMessage(
                        mediaUrl = downloadUrl.toString(),
                        mediaType = type,
                        senderId = currentUserId,
                        receiverId = targetUserId,
                        time = Timestamp.now()
                    )
                    db.collection("chats")
                        .document(chatDocumentId)
                        .collection("messages")
                        .add(message)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "$type 업로드 실패", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::messageListener.isInitialized) messageListener.remove()
    }
}
