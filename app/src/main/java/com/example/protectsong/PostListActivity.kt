package com.example.protectsong

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityPostListBinding

class PostListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: 게시글 목록 불러오기, 리사이클러뷰 연결 등 구현
    }
}
