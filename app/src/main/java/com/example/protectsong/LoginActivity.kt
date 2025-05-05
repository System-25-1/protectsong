// ✅ LoginActivity.kt
package com.example.protectsong

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔙 뒤로가기
        binding.backButton.setOnClickListener {
            finish()
        }

        // ✅ 로그인 버튼 클릭
        binding.loginSubmitButton.setOnClickListener {
            val studentId = binding.studentIdEdit.text.toString()
            val password = binding.passwordEdit.text.toString()

            // TODO: 로그인 처리 로직 추가
        }
    }
}
