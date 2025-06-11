package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.adapter.ChatListAdapter
import com.example.protectsong.databinding.ActivityChatListBinding
import com.example.protectsong.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatListBinding
    private lateinit var adapter: ChatListAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val studentChats = mutableListOf<ChatListItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ChatListAdapter(studentChats) { studentUid ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatWithUserId", studentUid)
            startActivity(intent)
        }

        binding.recyclerViewChatList.adapter = adapter
        binding.recyclerViewChatList.layoutManager = LinearLayoutManager(this)

        // 뒤로가기 텍스트 클릭 시 역할 따라 이동
        findViewById<TextView>(R.id.backText).setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .get()
                    .addOnSuccessListener { document ->
                        val role = document.getString("role")
                        val intent = if (role == "admin") {
                            Intent(this, AdminMainActivity::class.java)
                        } else {
                            Intent(this, MainActivity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        // 관리자 계정이면 채팅 리스트 로드
        auth.currentUser?.getIdToken(true)
            ?.addOnSuccessListener { result ->
                val isAdmin = result.claims["admin"] == true
                if (isAdmin) {
                    Log.d("🔥AdminCheck", "관리자 계정입니다.")
                    loadChatList()
                } else {
                    Log.e("🔥AdminCheck", "⚠ 관리자 권한이 없습니다.")
                }
            }

        // 하단 네비게이션 초기화
        binding.bottomNavigation.selectedItemId = R.id.nav_chat
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, AdminMainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_post -> {
                    startActivity(Intent(this, PostListActivity::class.java))
                    finish()
                    true
                }
                else -> true
            }
        }
    }

    private fun loadChatList() {
        db.collection("chats").get().addOnSuccessListener { snapshot ->
            val chatDocs = snapshot.documents
            Log.d("ChatList", "총 채팅방 수: ${chatDocs.size}")

            val tempList = mutableListOf<ChatListItem>()
            var remaining = chatDocs.size

            if (remaining == 0) {
                studentChats.clear()
                adapter.notifyDataSetChanged()
                return@addOnSuccessListener
            }

            for (doc in chatDocs) {
                val studentUid = doc.id

                db.collection("chats").document(studentUid)
                    .collection("messages")
                    .orderBy("time", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { messageSnapshot ->
                        if (!messageSnapshot.isEmpty) {
                            val latestMessage = messageSnapshot.documents.first().toObject(ChatMessage::class.java)

                            if (latestMessage != null) {
                                db.collection("users").document(studentUid).get()
                                    .addOnSuccessListener { userDoc ->
                                        val name = userDoc.getString("name") ?: "이름없음"
                                        val studentId = userDoc.getString("studentId") ?: "학번없음"

                                        tempList.add(ChatListItem(name, studentId, studentUid, latestMessage))
                                        remaining--

                                        if (remaining == 0) {
                                            studentChats.clear()
                                            studentChats.addAll(tempList.sortedByDescending { it.message.time })
                                            adapter.notifyDataSetChanged()
                                        }
                                    }
                            } else {
                                remaining--
                                if (remaining == 0) {
                                    studentChats.clear()
                                    studentChats.addAll(tempList.sortedByDescending { it.message.time })
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        } else {
                            Log.d("ChatList", "⛔ 메시지 없음: $studentUid")
                            remaining--
                            if (remaining == 0) {
                                studentChats.clear()
                                studentChats.addAll(tempList.sortedByDescending { it.message.time })
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
            }
        }.addOnFailureListener {
            Log.e("ChatList", "🔥 chats 전체 조회 실패", it)
        }
    }

    data class ChatListItem(
        val name: String,
        val studentId: String,
        val studentUid: String,
        val message: ChatMessage
    )
}
