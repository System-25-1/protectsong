package com.example.protectsong

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostAdapter(
    private val posts: List<Post>,
    private val onItemClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBadge: TextView = itemView.findViewById(R.id.tvBadge)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(post: Post) {
            // 제목, 날짜 설정
            tvTitle.text = post.title
            tvDate.text = post.date

            // 공지 뱃지 표시
            if (post.isNotice) {
                tvBadge.visibility = View.VISIBLE
            } else {
                tvBadge.visibility = View.GONE
            }

            // 클릭 리스너
            itemView.setOnClickListener {
                onItemClick(post)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size
}
