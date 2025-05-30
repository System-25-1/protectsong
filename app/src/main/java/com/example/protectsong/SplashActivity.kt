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
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = auth.currentUser
        if (user != null) {
            // 🔍 Firestore에서 role 확인
            firestore.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role")
                    Log.d("SplashActivity", "로그인된 사용자 role: $role")
                    val intent = if (role == "admin") {
                        Intent(this, AdminMainActivity::class.java)
                    } else {
                        Intent(this, MainActivity::class.java)
                    }
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e("SplashActivity", "Firestore role 조회 실패", e)
                    // 실패 시 일반 사용자용으로 기본 이동
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            return
        }

        // 📌 로그인 안 된 상태 → splash 애니메이션 + 로그인 화면 전환
        val scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_bounce)
        binding.splashText2.startAnimation(scaleAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.splashView.visibility = View.GONE
            binding.loginView.visibility = View.VISIBLE

            val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            binding.loginButton.startAnimation(slideUp)
            binding.signupButton.startAnimation(slideUp)
        }, 2000)

        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.signupButton.setOnClickListener {

            startActivity(Intent(this, SignupActivity::class.java))

            val intent = Intent(this, UserInfoActivity::class.java)
            startActivity(intent)

            finish()
        }

        Log.d("SplashActivity", "currentUser: $user")
    }
}
