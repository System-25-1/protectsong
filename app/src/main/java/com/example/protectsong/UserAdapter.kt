package com.example.protectsong.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.protectsong.R
import com.example.protectsong.model.User

class UserAdapter : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val users = mutableListOf<User>()

    fun submitList(newList: List<User>) {
        users.clear()
        users.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvStudentId: TextView = itemView.findViewById(R.id.tvStudentId)
        private val tvAdmin: TextView = itemView.findViewById(R.id.tvAdmin)

        fun bind(user: User) {
            tvName.text = "이름: ${user.name}"
            tvStudentId.text = "학번: ${user.studentId}"
            tvAdmin.text = if (user.isAdmin) "권한: 관리자" else "권한: 일반 사용자"
        }
    }
}
