package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기 버튼
        binding.backButton.setOnClickListener {
            finish()
        }

        // 회원가입 버튼 클릭
        binding.signupSubmitButton.setOnClickListener {
            val name = binding.nameEdit.text.toString()
            val email = binding.emailEdit.text.toString()
            val password = binding.passwordEdit.text.toString()

            // 여기에 회원가입 처리 로직 추가

            // UserInfoActivity로 이동
            val intent = Intent(this, UserInfoActivity::class.java)
            startActivity(intent)
        }
    }
}
