package com.example.protectsong.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.protectsong.databinding.ItemChatMeBinding
import com.example.protectsong.databinding.ItemChatOtherBinding
import com.example.protectsong.databinding.ItemDateHeaderBinding
import com.example.protectsong.model.ChatDisplayItem
import com.example.protectsong.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val items: List<ChatDisplayItem>,
    private val myId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_DATE = 0
        private const val VIEW_TYPE_ME = 1
        private const val VIEW_TYPE_OTHER = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is ChatDisplayItem.DateHeader -> VIEW_TYPE_DATE
            is ChatDisplayItem.MessageItem -> {
                if (item.message.senderId == myId) VIEW_TYPE_ME else VIEW_TYPE_OTHER
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_DATE -> {
                val binding = ItemDateHeaderBinding.inflate(inflater, parent, false)
                DateViewHolder(binding)
            }
            VIEW_TYPE_ME -> {
                val binding = ItemChatMeBinding.inflate(inflater, parent, false)
                MeViewHolder(binding)
            }
            VIEW_TYPE_OTHER -> {
                val binding = ItemChatOtherBinding.inflate(inflater, parent, false)
                OtherViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ChatDisplayItem.DateHeader -> {
                (holder as DateViewHolder).bind(item.dateText)
            }
            is ChatDisplayItem.MessageItem -> {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val timeText = timeFormat.format(item.message.time.toDate())

                if (holder is MeViewHolder) {
                    holder.binding.textMessage.text = item.message.text
                    holder.binding.textTime.text = timeText
                } else if (holder is OtherViewHolder) {
                    holder.binding.textMessage.text = item.message.text
                    holder.binding.textTime.text = timeText
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // 뷰홀더들
    inner class DateViewHolder(private val binding: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.dateText.text = date
        }
    }

    inner class MeViewHolder(val binding: ItemChatMeBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class OtherViewHolder(val binding: ItemChatOtherBinding) :
        RecyclerView.ViewHolder(binding.root)
}
