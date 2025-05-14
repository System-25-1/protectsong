package com.example.protectsong.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.protectsong.databinding.ItemChatMeBinding
import com.example.protectsong.databinding.ItemChatOtherBinding
import com.example.protectsong.model.ChatMessage

class ChatAdapter(private val items: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ME = 0
        private const val VIEW_TYPE_OTHER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isSent) VIEW_TYPE_ME else VIEW_TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ME) {
            val binding = ItemChatMeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            MeViewHolder(binding)
        } else {
            val binding = ItemChatOtherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            OtherViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = items[position]
        if (holder is MeViewHolder) {
            holder.binding.textMessage.text = message.text
            holder.binding.textTime.text = message.time
        } else if (holder is OtherViewHolder) {
            holder.binding.textMessage.text = message.text
            holder.binding.textTime.text = message.time
        }
    }

    override fun getItemCount(): Int = items.size

    inner class MeViewHolder(val binding: ItemChatMeBinding) : RecyclerView.ViewHolder(binding.root)
    inner class OtherViewHolder(val binding: ItemChatOtherBinding) : RecyclerView.ViewHolder(binding.root)
}
