package com.example.protectsong.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.protectsong.ChatListActivity.ChatListItem
import com.example.protectsong.databinding.ItemChatListBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatListAdapter(
    private val items: List<ChatListItem>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemChatListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ChatListItem) {
            // 이름 + 학번
            binding.textStudentId.text = "${data.name} (${data.studentId})"

            // ✅ 메시지 유형에 따라 미리보기 표시
            binding.textLastMessage.text = when {
                !data.message.mediaType.isNullOrEmpty() -> {
                    when (data.message.mediaType) {
                        "image" -> "사진"
                        "video" -> "동영상"
                        else -> "미디어"
                    }
                }
                !data.message.text.isNullOrBlank() -> data.message.text
                else -> ""
            }

            // ✅ 시간 또는 날짜 포맷
            val messageDate = data.message.time.toDate()
            val now = Date()
            val dateFormat = if (
                SimpleDateFormat("yyyyMMdd").format(messageDate) ==
                SimpleDateFormat("yyyyMMdd").format(now)
            ) {
                SimpleDateFormat("HH:mm", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            }
            binding.textTime.text = dateFormat.format(messageDate)

            // 클릭 리스너
            binding.root.setOnClickListener {
                onItemClick(data.studentUid)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}