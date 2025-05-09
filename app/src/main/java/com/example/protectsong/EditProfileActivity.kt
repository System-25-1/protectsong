package com.example.protectsong

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로 버튼 클릭
        binding.backText.setOnClickListener {
            finish()
        }
        // 예: 수정 버튼 클릭 처리
        binding.btnUpdate.setOnClickListener {
// 수정 처리 로직
        }
    }
}
