package com.example.protectsong

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var isWhistleOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnEmergency = findViewById<ImageButton>(R.id.btnEmergency)
        val btnWhistle = findViewById<ImageButton>(R.id.btnWhistle)
        val tvWhistle = findViewById<TextView>(R.id.tvWhistle)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val ivCall = findViewById<ImageView>(R.id.ivCall) // ✅ 전화신고 버튼 참조

        btnEmergency.setOnClickListener {
            Toast.makeText(this, "긴급 신고 버튼이 눌렸습니다!", Toast.LENGTH_SHORT).show()
        }

        btnWhistle.setOnClickListener {
            isWhistleOn = !isWhistleOn
            btnWhistle.isSelected = isWhistleOn
            tvWhistle.text = if (isWhistleOn) "on" else "off"
        }

        ivCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:010-8975-0220") // ✅ 관재팀 전화번호로 수정
            startActivity(intent)
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    Toast.makeText(this, "Chat 탭 선택됨", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_home -> {
                    Toast.makeText(this, "Home 탭 선택됨", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_post -> {
                    Toast.makeText(this, "Post 탭 선택됨", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}
