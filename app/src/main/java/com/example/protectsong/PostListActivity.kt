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

        // < 이전
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

        // 페이지 번호
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

        // 다음 >
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

            val filtered = allNormalPosts.filter {
                it.title.contains(keyword, ignoreCase = true) ||
                        it.content.contains(keyword, ignoreCase = true)
            }

            postList.clear()
            postList.addAll(allNotices)
            postList.addAll(filtered)
            postAdapter.notifyDataSetChanged()

            if (filtered.isEmpty()) {
                Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
            }
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
                        db.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.getString("role") == "admin") {
                                    startActivity(Intent(this, AdminMainActivity::class.java))
                                } else {
                                    startActivity(Intent(this, MainActivity::class.java))
                                }
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
