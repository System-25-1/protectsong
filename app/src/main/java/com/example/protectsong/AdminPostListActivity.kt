package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityAdminPostListBinding
import com.google.firebase.Timestamp

class AdminPostListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPostListBinding
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPostListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 글쓰기 Activity 실행 후 결과 처리
        val writePostLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val newPost = result.data?.getSerializableExtra("newPost") as? Post
                newPost?.let {
                    postList.add(0, it) // 새 게시글을 리스트 맨 위에 추가
                    postAdapter.notifyItemInserted(0)
                    binding.recyclerViewPosts.scrollToPosition(0)
                }
            }
        }

        // ✅ 글쓰기 버튼 클릭 시
        binding.btnWritePost.setOnClickListener {
            val intent = Intent(this, PostWriteActivity::class.java)
            writePostLauncher.launch(intent)
        }

        // ✅ 초기 더미 게시글 추가
        postList.addAll(
            listOf(
                Post(
                    id = "1",
                    title = "명신관 610호 천장 낙하 주의",
                    content = "주의 바랍니다.",
                    category = "공지",
                    timestamp = Timestamp.now()
                ),
                Post(
                    id = "2",
                    title = "외부인 출입 금지 조항",
                    content = "외부인 출입 금지 관련 안내",
                    category = "공지",
                    timestamp = Timestamp.now()
                ),
                Post(
                    id = "3",
                    title = "앱 오류 사항 문의 방법",
                    content = "관리자에게 문의해주세요.",
                    category = "공지",
                    timestamp = Timestamp.now()
                )
            )
        )

        // ✅ RecyclerView + 어댑터 연결
        postAdapter = PostAdapter(postList) { post ->
            val intent = Intent(this, PostDetailActivity::class.java)
            intent.putExtra("postId", post.id)
            startActivity(intent)
        }
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPosts.adapter = postAdapter

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
}
