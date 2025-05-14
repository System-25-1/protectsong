package com.example.protectsong

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityPostDetailBinding

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 전달된 데이터 받아오기
        val title = intent.getStringExtra("title") ?: "제목 없음"
        val date = intent.getStringExtra("date") ?: "날짜 없음"
        val isNotice = intent.getBooleanExtra("isNotice", false)

        // UI에 데이터 세팅
        binding.tvTitle.text = title
        binding.tvDate.text = date
        binding.tvBadge.text = if (isNotice) "공지" else ""
        binding.tvBadge.visibility = if (isNotice) android.view.View.VISIBLE else android.view.View.GONE
    }
}
