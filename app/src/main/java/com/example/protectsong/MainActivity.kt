package com.example.protectsong

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isWhistleOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🟦 툴바 및 Drawer 설정
        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 툴바 타이틀 중앙 설정
        binding.toolbarTitle.text = "지키송"

        // NavigationView 메뉴 클릭 처리
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_mypage -> {
                    Toast.makeText(this, "마이페이지 클릭됨", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "설정 클릭됨", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_logout -> {
                    Toast.makeText(this, "로그아웃 클릭됨", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // 긴급 신고 버튼
        binding.btnEmergency.setOnClickListener {
            Toast.makeText(this, "긴급 신고 버튼이 눌렸습니다!", Toast.LENGTH_SHORT).show()
        }

        // 호루라기 버튼
        binding.btnWhistle.setOnClickListener {
            isWhistleOn = !isWhistleOn
            binding.btnWhistle.isSelected = isWhistleOn
            binding.tvWhistle.text = if (isWhistleOn) "on" else "off"
        }

        // 전화 신고 버튼
        binding.ivCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:010-8975-0220")
            startActivity(intent)
        }

        // 하단 바 메뉴 클릭
        binding.bottomNavigation.setOnItemSelectedListener { item ->
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
