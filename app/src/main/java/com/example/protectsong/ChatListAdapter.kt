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
            binding.textStudentId.text = "${data.name} (${data.studentId})"
            binding.textLastMessage.text = data.message.text

            // ✅ 날짜 비교: 오늘이면 시간(HH:mm), 아니면 날짜(yyyy.MM.dd)
            val messageDate = data.message.time.toDate()
            val now = Date()

            val dateFormat = if (SimpleDateFormat("yyyyMMdd").format(messageDate) ==
                SimpleDateFormat("yyyyMMdd").format(now)) {
                SimpleDateFormat("HH:mm", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            }

            binding.textTime.text = dateFormat.format(messageDate)

            binding.root.setOnClickListener {
                onItemClick(data.studentUid)  // ✅ 항상 학생 UID 넘김
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
