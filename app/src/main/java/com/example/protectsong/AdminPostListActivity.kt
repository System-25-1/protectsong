package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityAdminPostListBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminPostListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPostListBinding
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()

    private val db = FirebaseFirestore.getInstance()

    // ✅ 글쓰기 결과 처리
    private val writePostLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            loadPostsFromFirestore()
        }
    }

    // ✅ 게시글 상세 보기 결과 처리 (수정/삭제)
    private val detailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val deleted = result.data?.getBooleanExtra("postDeleted", false) ?: false
            val updated = result.data?.getBooleanExtra("postUpdated", false) ?: false
            if (deleted || updated) {
                loadPostsFromFirestore()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPostListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 글쓰기 버튼
        binding.btnWritePost.setOnClickListener {
            val intent = Intent(this, AdminPostWriteActivity::class.java)
            writePostLauncher.launch(intent)
        }

        // ✅ RecyclerView + 어댑터 연결
        postAdapter = PostAdapter(postList) { post ->
            val intent = Intent(this, PostDetailActivity::class.java)
            intent.putExtra("postId", post.id)
            detailLauncher.launch(intent)
        }
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPosts.adapter = postAdapter

        // ✅ Firestore에서 초기 데이터 로딩
        loadPostsFromFirestore()

        // ✅ 하단 네비게이션
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

    // ✅ Firestore에서 게시글 목록 불러오기
    private fun loadPostsFromFirestore() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                postList.clear()
                for (document in result) {
                    val post = document.toObject(Post::class.java)
                    postList.add(post)
                }
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                // 에러 처리 필요 시 토스트 추가
            }
    }
}
