package com.example.protectsong

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.protectsong.databinding.ItemLogBinding
import com.example.protectsong.model.LogEntry
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class LogListAdapter(private val logs: List<LogEntry>) : ListAdapter<LogEntry, LogListAdapter.LogViewHolder>(DIFF_CALLBACK) {

    inner class LogViewHolder(private val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(log: LogEntry) {
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(log.userId)
                .get()
                .addOnSuccessListener { userDoc ->
                    val name = userDoc.getString("name") ?: log.userId
                    binding.tvUserId.text = name
                }
                .addOnFailureListener {
                    binding.tvUserId.text = log.userId
                }

            binding.tvAction.text = log.action
            binding.tvDetail.text = log.detail
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            binding.tvTimestamp.text = sdf.format(Date(log.timestamp))
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LogEntry>() {
            override fun areItemsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean = oldItem.timestamp == newItem.timestamp
            override fun areContentsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean = oldItem == newItem
        }
    }
}