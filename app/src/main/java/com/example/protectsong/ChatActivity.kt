package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityChatBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.protectsong.adapter.ChatAdapter
import com.example.protectsong.model.ChatMessage


class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 날짜 표시
        binding.chatStartDate.text = SimpleDateFormat("yyyy.MM.dd.E", Locale.getDefault()).format(Date())

        // RecyclerView
        adapter = ChatAdapter(messages)
        binding.recyclerViewChat.adapter = adapter
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(this)

        // 메시지 전송
        binding.btnSend.setOnClickListener {
            val text = binding.editMessage.text.toString()
            if (text.isNotBlank()) {
                val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                messages.add(ChatMessage(text, timestamp, isSent = true))
                adapter.notifyItemInserted(messages.size - 1)
                binding.editMessage.text.clear()
                binding.recyclerViewChat.scrollToPosition(messages.size - 1)
            }
        }

        // + 버튼으로 첨부 메뉴 토글
        var isExpanded = false
        binding.btnToggleAttachment.setOnClickListener {
            isExpanded = !isExpanded
            binding.attachmentLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.btnToggleAttachment.setImageResource(
                if (isExpanded) R.drawable.ic_minus else R.drawable.ic_plus
            )
        }

        // 사진/영상/카메라 클릭 이벤트 (추후 구현)
        binding.btnImage.setOnClickListener { /* 갤러리 연동 */ }
        binding.btnVideo.setOnClickListener { /* 비디오 연동 */ }
        binding.btnCamera.setOnClickListener { /* 카메라 실행 */ }
        binding.bottomNavigation.selectedItemId = R.id.nav_chat

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> true
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_post -> {
                    startActivity(Intent(this, PostListActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }
}
