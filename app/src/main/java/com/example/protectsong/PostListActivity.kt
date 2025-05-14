package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityPostListBinding

class PostListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 더미 데이터 준비
        val dummyPosts = listOf(
            Post("명신관 610호 천장 낙하 주의", "2025.04.11", true),
            Post("외부인 출입 금지 조항", "2025.01.16", true),
            Post("허위 신고 조항 안내", "2025.01.09", true),
            Post("앱 오류 사항 문의 방법", "2024.11.25", true),
            // 일반 게시글도 테스트용으로 추가 가능
            Post("학생 건의사항 접수 안내", "2025.03.01", false),
            Post("설문조사 참여 부탁드립니다", "2025.02.20", false)
        )

        // 2. 리사이클러뷰 레이아웃 매니저 설정
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(this)

        // 3. 어댑터 설정
        val postAdapter = PostAdapter(dummyPosts) { post ->
            // 게시글 클릭 시 동작 (예: 상세 페이지 이동)
            val intent = Intent(this, PostDetailActivity::class.java)
            intent.putExtra("title", post.title)
            intent.putExtra("date", post.date)
            intent.putExtra("isNotice", post.isNotice)
            startActivity(intent)
        }

        binding.recyclerViewPosts.adapter = postAdapter

        // 4. 하단 바에서 현재 탭 선택 표시
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
                R.id.nav_post -> {
                    true
                }
                else -> false
            }
        }
    }
}
