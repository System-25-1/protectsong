package com.example.protectsong.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.protectsong.VideoPlayerActivity
import com.example.protectsong.databinding.ItemChatMeBinding
import com.example.protectsong.databinding.ItemChatOtherBinding
import com.example.protectsong.databinding.ItemDateHeaderBinding
import com.example.protectsong.model.ChatDisplayItem
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
            VIEW_TYPE_DATE -> DateViewHolder(ItemDateHeaderBinding.inflate(inflater, parent, false))
            VIEW_TYPE_ME -> MeViewHolder(ItemChatMeBinding.inflate(inflater, parent, false))
            VIEW_TYPE_OTHER -> OtherViewHolder(ItemChatOtherBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ChatDisplayItem.DateHeader -> {
                (holder as DateViewHolder).bind(item.dateText)
            }
            is ChatDisplayItem.MessageItem -> {
                val msg = item.message
                val timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(msg.time.toDate())

                when (holder) {
                    is MeViewHolder -> holder.bind(msg.text, msg.mediaUrl, msg.mediaType, timeText)
                    is OtherViewHolder -> holder.bind(msg.text, msg.mediaUrl, msg.mediaType, timeText)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class DateViewHolder(private val binding: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.dateText.text = date
        }
    }

    inner class MeViewHolder(val binding: ItemChatMeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String, mediaUrl: String?, mediaType: String?, time: String) {
            binding.textTime.text = time

            when (mediaType) {
                "image" -> {
                    binding.imageView.visibility = View.VISIBLE
                    binding.videoThumbnail.visibility = View.GONE
                    binding.textMessage.visibility = View.GONE
                    Glide.with(binding.imageView.context)
                        .load(mediaUrl)
                        .into(binding.imageView)
                }
                "video" -> {
                    binding.videoThumbnail.visibility = View.VISIBLE
                    binding.imageView.visibility = View.GONE
                    binding.textMessage.visibility = View.GONE
                    Glide.with(binding.videoThumbnail.context)
                        .load(mediaUrl)
                        .into(binding.videoThumbnail)
                    binding.videoThumbnail.setOnClickListener {
                        val intent = Intent(binding.root.context, VideoPlayerActivity::class.java)
                        intent.putExtra("videoUrl", mediaUrl)
                        binding.root.context.startActivity(intent)
                    }
                }
                else -> {
                    binding.textMessage.text = text
                    binding.textMessage.visibility = View.VISIBLE
                    binding.imageView.visibility = View.GONE
                    binding.videoThumbnail.visibility = View.GONE
                }
            }
        }
    }

    inner class OtherViewHolder(val binding: ItemChatOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String, mediaUrl: String?, mediaType: String?, time: String) {
            binding.textTime.text = time

            when (mediaType) {
                "image" -> {
                    binding.imageView.visibility = View.VISIBLE
                    binding.videoThumbnail.visibility = View.GONE
                    binding.textMessage.visibility = View.GONE
                    Glide.with(binding.imageView.context)
                        .load(mediaUrl)
                        .into(binding.imageView)
                }
                "video" -> {
                    binding.videoThumbnail.visibility = View.VISIBLE
                    binding.imageView.visibility = View.GONE
                    binding.textMessage.visibility = View.GONE
                    Glide.with(binding.videoThumbnail.context)
                        .load(mediaUrl)
                        .into(binding.videoThumbnail)
                    binding.videoThumbnail.setOnClickListener {
                        val intent = Intent(binding.root.context, VideoPlayerActivity::class.java)
                        intent.putExtra("videoUrl", mediaUrl)
                        binding.root.context.startActivity(intent)
                    }
                }
                else -> {
                    binding.textMessage.text = text
                    binding.textMessage.visibility = View.VISIBLE
                    binding.imageView.visibility = View.GONE
                    binding.videoThumbnail.visibility = View.GONE
                }
            }
        }
    }
}
