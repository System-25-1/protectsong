package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityPostListBinding
import com.example.protectsong.Post
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PostListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostListBinding
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        initBottomNavigation()
        loadPostsFromFirestore()
        setupSearch()
    }

    private fun initRecyclerView() {
        postAdapter = PostAdapter(postList) { post ->
            val intent = Intent(this, PostDetailActivity::class.java)
            intent.putExtra("postId", post.id)
            startActivity(intent)
        }

        binding.recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(this@PostListActivity)
            adapter = postAdapter
            setHasFixedSize(true)
        }
    }

    private fun initBottomNavigation() {
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

    private fun loadPostsFromFirestore() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val notices = mutableListOf<Post>()
                val others = mutableListOf<Post>()

                for (document in result) {
                    val post = document.toObject(Post::class.java)
                    if (post.isNotice) {
                        notices.add(post)
                    } else {
                        others.add(post)
                    }
                }

                postList.clear()
                postList.addAll(notices)
                postList.addAll(others)
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "게시글을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupSearch() {
        binding.btnSearch.setOnClickListener {
            val keyword = binding.etSearch.text.toString().trim()

            val filtered = if (keyword.isEmpty()) {
                postList
            } else {
                postList.filter {
                    it.title.contains(keyword, ignoreCase = true) ||
                            it.content.contains(keyword, ignoreCase = true)
                }
            }

            postAdapter.updateData(filtered.toMutableList())

            if (filtered.isEmpty()) {
                Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
