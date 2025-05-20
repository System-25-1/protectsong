package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityPostListBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostListBinding
    private lateinit var postAdapter: PostAdapter
    private val allPosts = mutableListOf<Post>()  // 전체 게시글 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 글쓰기 버튼 기본 숨김
        binding.btnWritePost.visibility = View.GONE

        // 🔹 관리자만 글쓰기 버튼 보이게
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseFirestore.getInstance().collection("users").document(uid ?: return)
            .get()
            .addOnSuccessListener { document ->
                if (document.getString("role") == "admin") {
                    binding.btnWritePost.visibility = View.VISIBLE
                    binding.btnWritePost.setOnClickListener {
                        startActivity(Intent(this, AdminPostWriteActivity::class.java))
                    }
                }
            }


        // 🔹 Firestore에서 게시글 불러오기
        FirebaseFirestore.getInstance().collection("posts")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                allPosts.clear()
                for (doc in result) {
                    val post = Post(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        category = doc.getString("category") ?: "일반",
                        timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                    )
                    allPosts.add(post)
                }
                postAdapter.updateData(allPosts)
            }

        // 🔹 어댑터 설정
        postAdapter = PostAdapter(allPosts) { post ->
            val intent = Intent(this, PostDetailActivity::class.java)
            intent.putExtra("postId", post.id)
            startActivity(intent)
        }

        binding.recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(this@PostListActivity)
            adapter = postAdapter
        }

        // 🔍 검색 버튼 클릭
        binding.btnSearch.setOnClickListener {
            val keyword = binding.etSearch.text.toString().trim()
            if (keyword.isEmpty()) {
                postAdapter.updateData(allPosts)
            } else {
                val filtered = allPosts.filter {
                    it.title.contains(keyword, ignoreCase = true)
                }
                postAdapter.updateData(filtered)
            }
        }

        // 🔹 하단 바 설정
        binding.bottomNavigation.selectedItemId = R.id.nav_post
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    true
                }
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_post -> true
                else -> false
            }
        }
    }
}