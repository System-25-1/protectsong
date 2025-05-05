package com.example.protectsong

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 애니메이션 적용 (지키송 텍스트에)
        val scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_bounce)
        binding.splashText2.startAnimation(scaleAnim)

        // 1.5초 후: splashView 숨기고 loginView 표시
        Handler(Looper.getMainLooper()).postDelayed({
            binding.splashView.visibility = View.GONE
            binding.loginView.visibility = View.VISIBLE

            // 애니메이션 적용
            val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            binding.loginButton.startAnimation(slideUp)
            binding.signupButton.startAnimation(slideUp)

        }, 2000)
        // 로그인 버튼 클릭 처리
        binding.loginButton.setOnClickListener {
            // TODO: 로그인 화면 이동
        }

        // 회원가입 버튼 클릭 처리
        binding.signupButton.setOnClickListener {
            // TODO: 회원가입 화면 이동
        }
    }
}
