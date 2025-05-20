package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ChatAdapter
    private lateinit var messageListener: ListenerRegistration

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val adminUid = "MecPxatzCTMeHztzELY4ps4KVeh2"

    private val currentUserId: String by lazy {
        auth.currentUser?.uid ?: ""
    }

    private val targetUserId: String by lazy {
        if (currentUserId == adminUid) {
            intent.getStringExtra("chatWithUserId") ?: ""
        } else {
            adminUid
        }
    }

    private val chatDocumentId: String by lazy {
        if (currentUserId == adminUid) targetUserId else currentUserId
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("🔥Auth", "현재 로그인한 UID: ${auth.currentUser?.uid}")
        Log.d("🔥DEBUG", "currentUserId = $currentUserId")
        Log.d("🔥DEBUG", "targetUserId = $targetUserId")
        Log.d("🔥DEBUG", "chatDocumentId = $chatDocumentId")

        adapter = ChatAdapter(emptyList(), currentUserId)
        binding.recyclerViewChat.adapter = adapter
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(this)

        binding.btnSend.setOnClickListener {
            val text = binding.editMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }

        var isExpanded = false
        binding.btnToggleAttachment.setOnClickListener {
            isExpanded = !isExpanded
            binding.attachmentLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.btnToggleAttachment.setImageResource(
                if (isExpanded) R.drawable.ic_minus else R.drawable.ic_plus
            )
        }

        binding.btnImage.setOnClickListener { Toast.makeText(this, "사진 기능 준비 중", Toast.LENGTH_SHORT).show() }
        binding.btnVideo.setOnClickListener { Toast.makeText(this, "동영상 기능 준비 중", Toast.LENGTH_SHORT).show() }
        binding.btnCamera.setOnClickListener { Toast.makeText(this, "카메라 기능 준비 중", Toast.LENGTH_SHORT).show() }

        binding.bottomNavigation.selectedItemId = R.id.nav_chat
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_post -> {
                    startActivity(Intent(this, PostListActivity::class.java))
                    true
                }
                R.id.nav_chat -> true
                else -> false
            }
        }

        listenForMessages()
    }

    private fun sendMessage(text: String) {
        val message = ChatMessage(
            text = text,
            time = Timestamp.now(),
            senderId = currentUserId,
            receiverId = targetUserId
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
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                val rawMessages = mutableListOf<ChatMessage>()
                for (doc in snapshots.documents) {
                    val message = doc.toObject(ChatMessage::class.java)
                    if (message != null &&
                        ((message.senderId == currentUserId && message.receiverId == targetUserId) ||
                                (message.senderId == targetUserId && message.receiverId == currentUserId))) {
                        rawMessages.add(message)
                    }
                }

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

    override fun onDestroy() {
        super.onDestroy()
        if (::messageListener.isInitialized) {
            messageListener.remove()
        }
    }
}
