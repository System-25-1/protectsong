package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 로그인 상태 체크
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // 이미 로그인된 상태면 바로 MainActivity로 이동
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // ✅ 애니메이션 적용 (지키송 텍스트에)
        val scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_bounce)
        binding.splashText2.startAnimation(scaleAnim)

        // ✅ 2초 후 splashView 숨기고 loginView 보여주기
        Handler(Looper.getMainLooper()).postDelayed({
            binding.splashView.visibility = View.GONE
            binding.loginView.visibility = View.VISIBLE

            val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            binding.loginButton.startAnimation(slideUp)
            binding.signupButton.startAnimation(slideUp)
        }, 2000)

        // ✅ 로그인 버튼 클릭 → LoginActivity 이동 + 현재 액티비티 종료
        binding.loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // ✅ 회원가입 버튼 클릭 → SignupActivity 이동 + 현재 액티비티 종료
        binding.signupButton.setOnClickListener {
            val intent = Intent(this, UserInfoActivity::class.java)
            startActivity(intent)
            finish()
        }
        Log.d("SplashActivity", "currentUser: ${FirebaseAuth.getInstance().currentUser}")

    }

}
