package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityPostListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PostListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostListBinding
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private val db = FirebaseFirestore.getInstance()

    // ✅ 글쓰기 결과 처리 (작성 성공 시 새로고침)
    private val writePostLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            loadPostsFromFirestore()
        }
    }

    // ✅ 게시글 상세 결과 처리 (삭제 또는 수정 시 새로고침)
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
        binding = ActivityPostListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        initBottomNavigation()
        loadPostsFromFirestore()
        setupSearch()

        // ✅ 글쓰기 버튼 기본 숨김
        binding.btnWritePost.visibility = View.GONE

        // ✅ 관리자일 경우 글쓰기 버튼 표시
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role")
                    if (role == "admin") {
                        binding.btnWritePost.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
        }

        // ✅ 글쓰기 버튼 클릭 시
        binding.btnWritePost.setOnClickListener {
            val intent = Intent(this, AdminPostWriteActivity::class.java)
            writePostLauncher.launch(intent)
        }
    }

    private fun initRecyclerView() {
        postAdapter = PostAdapter(postList) { post ->
            val intent = Intent(this, PostDetailActivity::class.java)
            intent.putExtra("postId", post.id)
            detailLauncher.launch(intent)  // ✅ 수정/삭제 결과를 감지
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
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        FirebaseFirestore.getInstance().collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                val role = document.getString("role")
                                if (role == "admin") {
                                    startActivity(Intent(this, AdminMainActivity::class.java))
                                } else {
                                    startActivity(Intent(this, MainActivity::class.java))
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                            }
                    }
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
