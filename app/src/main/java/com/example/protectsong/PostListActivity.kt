package com.example.protectsong

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
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

    private val allNotices = mutableListOf<Post>()
    private val allNormalPosts = mutableListOf<Post>()
    private var currentPage = 1
    private val itemsPerPage = 10
    private var totalPages = 1

    private val writePostLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            loadPostsFromFirestore()
        }
    }

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

        findViewById<TextView>(R.id.backText).setOnClickListener {
            startActivity(Intent(this, AdminMainActivity::class.java))
            finish()
        }
        binding.btnWritePost.visibility = View.GONE
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.getString("role") == "admin") {
                        binding.btnWritePost.visibility = View.VISIBLE
                    }
                }
        }

        binding.btnWritePost.setOnClickListener {
            val intent = Intent(this, AdminPostWriteActivity::class.java)
            writePostLauncher.launch(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadPostsFromFirestore()
    }

    private fun initRecyclerView() {
        postAdapter = PostAdapter(postList) { post ->
            val intent = Intent(this, PostDetailActivity::class.java)
            intent.putExtra("postId", post.id)
            detailLauncher.launch(intent)
        }

        binding.recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(this@PostListActivity)
            adapter = postAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadPostsFromFirestore() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                allNotices.clear()
                allNormalPosts.clear()

                for (document in result) {
                    val post = document.toObject(Post::class.java)
                    if (post.isNotice) allNotices.add(post)
                    else allNormalPosts.add(post)
                }

                currentPage = 1
                totalPages = (allNormalPosts.size + itemsPerPage - 1) / itemsPerPage
                displayPage(currentPage)
                drawPagination()
            }
            .addOnFailureListener {
                Toast.makeText(this, "게시글을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayPage(page: Int) {
        val fromIndex = (page - 1) * itemsPerPage
        val toIndex = minOf(fromIndex + itemsPerPage, allNormalPosts.size)
        val currentPosts = if (fromIndex < allNormalPosts.size) allNormalPosts.subList(fromIndex, toIndex) else emptyList()

        postList.clear()
        postList.addAll(allNotices)
        postList.addAll(currentPosts)
        postAdapter.notifyDataSetChanged()
    }

    private fun drawPagination() {
        val layout = binding.paginationLayout
        layout.removeAllViews()

        if (currentPage > 1) {
            val prev = TextView(this).apply {
                text = "< 이전"
                textSize = 16f
                setPadding(20, 0, 20, 0)
                setTextColor(Color.parseColor("#002366"))
                minWidth = 100
                gravity = android.view.Gravity.CENTER
                setOnClickListener {
                    currentPage--
                    displayPage(currentPage)
                    drawPagination()
                }
            }
            layout.addView(prev)
        }

        for (i in 1..totalPages) {
            val tv = TextView(this).apply {
                text = "$i"
                textSize = 16f
                setPadding(12, 0, 12, 0)
                minWidth = 48
                gravity = android.view.Gravity.CENTER
                setTextColor(if (i == currentPage) Color.BLUE else Color.DKGRAY)
                setOnClickListener {
                    currentPage = i
                    displayPage(i)
                    drawPagination()
                }
            }
            layout.addView(tv)
        }

        if (currentPage < totalPages) {
            val next = TextView(this).apply {
                text = "다음 >"
                textSize = 16f
                setPadding(20, 0, 0, 0)
                minWidth = 100
                gravity = android.view.Gravity.CENTER
                setTextColor(Color.parseColor("#002366"))
                setOnClickListener {
                    currentPage++
                    displayPage(currentPage)
                    drawPagination()
                }
            }
            layout.addView(next)
        }
    }

    private fun setupSearch() {
        binding.btnSearch.setOnClickListener {
            val keyword = binding.etSearch.text.toString().trim()
            if (keyword.isEmpty()) {
                displayPage(currentPage)
                drawPagination()
                return@setOnClickListener
            }

            val filteredNotices = allNotices.filter {
                it.title.contains(keyword, ignoreCase = true) ||
                        it.content.contains(keyword, ignoreCase = true)
            }

            val filteredNormal = allNormalPosts.filter {
                it.title.contains(keyword, ignoreCase = true) ||
                        it.content.contains(keyword, ignoreCase = true)
            }

            postList.clear()
            postList.addAll(filteredNotices)
            postList.addAll(filteredNormal)
            postAdapter.notifyDataSetChanged()

            if (filteredNotices.isEmpty() && filteredNormal.isEmpty()) {
                Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_post
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        db.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                val role = document.getString("role")
                                val intent = if (role == "admin") {
                                    Intent(this, ChatListActivity::class.java) // ✅ 관리자 → 채팅목록
                                } else {
                                    Intent(this, ChatActivity::class.java)     // ✅ 학생 → 1:1 채팅
                                }
                                startActivity(intent)
                                finish() // 기존 액티비티 종료 (메모리 누적 방지)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "권한 확인 실패", Toast.LENGTH_SHORT).show()
                            }
                    }
                    true
                }

                R.id.nav_home -> {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        db.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.getString("role") == "admin") {
                                    startActivity(Intent(this, AdminMainActivity::class.java))
                                } else {
                                    startActivity(Intent(this, MainActivity::class.java))
                                }
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "권한 확인 실패", Toast.LENGTH_SHORT).show()
                            }
                    }
                    true
                }

                R.id.nav_post -> true
                else -> false
            }
        }
    }
}
