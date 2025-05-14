package com.example.protectsong

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var isWhistleOn = false
    private lateinit var whistlePlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바 설정
        setSupportActionBar(binding.toolbar)

        // 네비게이션 드로어 토글 설정
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // ✅ 네비게이션 헤더 내 버튼들 초기화
        val headerView = binding.navView.getHeaderView(0)
        val tvMyProfile = headerView.findViewById<TextView>(R.id.tvMyProfile)
        val logoutButton = headerView.findViewById<TextView>(R.id.logout_button)
        val btnSettings = headerView.findViewById<TextView>(R.id.btn_settings) // 🔥 헤더 설정 버튼

        // 🔁 로그아웃
        logoutButton.setOnClickListener {
            val intent = Intent(this, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // 🔁 내 정보 이동
        tvMyProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // ✅ 헤더의 설정 버튼 클릭 시 SettingsActivity 이동
        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            binding.drawerLayout.closeDrawer(binding.navView)
        }

        // ✅ 네비게이션 메뉴 항목 클릭 처리
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_mypage -> {
                    Toast.makeText(this, "마이페이지 클릭됨", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    binding.drawerLayout.closeDrawer(binding.navView)
                    true
                }

                R.id.nav_logout -> {
                    val intent = Intent(this, SplashActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        binding.btnSmsReport.setOnClickListener {
            val intent = Intent(this, SmsReportActivity::class.java)
            startActivity(intent)
        }


        // 🔊 호루라기 소리 준비
        whistlePlayer = MediaPlayer.create(this, R.raw.whistle_sound)

        // 🔘 호루라기 버튼 클릭
        binding.btnWhistle.setOnClickListener {
            isWhistleOn = !isWhistleOn

            binding.tvWhistle.text = if (isWhistleOn) "on" else "off"

            val backgroundRes = if (isWhistleOn) {
                R.drawable.bg_rectangle_button_pressed
            } else {
                R.drawable.bg_rectangle_button
            }
            binding.btnWhistle.setBackgroundResource(backgroundRes)

            if (isWhistleOn) {
                whistlePlayer.start()
            } else {
                if (whistlePlayer.isPlaying) {
                    whistlePlayer.pause()
                    whistlePlayer.seekTo(0)
                }
            }
        }

        // ☎ 전화 신고 버튼 클릭 → 다이얼 화면
        binding.ivCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:010-8975-0220")
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        whistlePlayer.release()
    }
}
