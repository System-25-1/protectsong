package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityPostListBinding
import com.google.firebase.Timestamp

class PostListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 더미 데이터 (전역 Post.kt에 맞춰 작성)
        val dummyPosts = listOf(
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
            ),
            Post(
                id = "4",
                title = "학생 건의사항 접수 안내",
                content = "의견을 제출해주세요.",
                category = "일반",
                timestamp = Timestamp.now()
            ),
            Post(
                id = "5",
                title = "설문조사 참여 부탁드립니다",
                content = "학교 생활 만족도 조사 참여 요청",
                category = "일반",
                timestamp = Timestamp.now()
            )
        )

        // 리사이클러뷰 설정
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(this)
        val postAdapter = PostAdapter(dummyPosts) { post ->
            val intent = Intent(this, PostDetailActivity::class.java)
            intent.putExtra("postId", post.id)
            startActivity(intent)
        }
        binding.recyclerViewPosts.adapter = postAdapter

        // 하단 바 초기화
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
