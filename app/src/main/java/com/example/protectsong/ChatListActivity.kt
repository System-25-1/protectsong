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

    private val adminUid = "Os1oJCzG45OKwyglRdc0JXxbghw2"
    private val studentChats = mutableListOf<ChatListItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Log.d("🔥Auth", "현재 로그인한 UID: ${auth.currentUser?.uid}")

        adapter = ChatListAdapter(studentChats) { studentUid ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatWithUserId", studentUid)
            startActivity(intent)
        }

        binding.recyclerViewChatList.adapter = adapter
        binding.recyclerViewChatList.layoutManager = LinearLayoutManager(this)

        // ✅ 뒤로 버튼 → MainActivity로 이동
        findViewById<TextView>(R.id.backText).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


        loadChatList()

        binding.bottomNavigation.selectedItemId = R.id.nav_chat
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, AdminMainActivity::class.java))
                    true
                }
                R.id.nav_post -> {
                    startActivity(Intent(this, PostListActivity::class.java))
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
