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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var isWhistleOn = false
    private lateinit var whistlePlayer: MediaPlayer
    private val ADMIN_UID = "MecPxatzCTMeHztzELY4ps4KVeh2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 툴바 설정
        setSupportActionBar(binding.toolbar)

        // ✅ 드로어 토글 설정
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // ✅ 드로어 헤더 버튼 초기화
        val headerView = binding.navView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvStudentId = headerView.findViewById<TextView>(R.id.tvStudentId)
        val tvMyProfile = headerView.findViewById<TextView>(R.id.tvMyProfile)
        val logoutButton = headerView.findViewById<TextView>(R.id.logout_button)

        // ✅ Firebase에서 사용자 정보 불러오기
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name") ?: "이름없음"
                    val studentId = document.getString("studentId") ?: "학번없음"
                    tvUserName.text = name
                    tvStudentId.text = studentId
                }
                .addOnFailureListener {
                    Toast.makeText(this, "사용자 정보를 불러오지 못했습니다", Toast.LENGTH_SHORT).show()
                }
        }

        // ✅ 로그아웃
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        // ✅ 프로필 수정 이동
        tvMyProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
        // ✅ 드로어 메뉴 선택
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
                    val intent = Intent(this, SplashActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        // ✅ 문자 신고 버튼
        binding.btnSmsReport.setOnClickListener {
            val intent = Intent(this, SmsReportActivity::class.java)
            startActivity(intent)
        }

        // ✅ 호루라기 기능
        whistlePlayer = MediaPlayer.create(this, R.raw.whistle_sound)

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

        // ✅ 전화 신고 버튼
        binding.ivCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:010-8975-0220")
            startActivity(intent)
        }



    // 하단 네비게이션 바

        // ✅ 하단 네비게이션 바

        binding.bottomNavigation.selectedItemId = R.id.nav_home

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnItemSelectedListener true
                    FirebaseFirestore.getInstance().collection("users").document(uid)
                        .get()
                        .addOnSuccessListener { doc ->
                            val role = doc.getString("role")
                            if (role == "admin") {
                                startActivity(Intent(this, ChatListActivity::class.java)) // 관리자: 채팅목록
                            } else {
                                val intent = Intent(this, ChatActivity::class.java)       // 학생: 관리자와 채팅
                                intent.putExtra("chatWithUserId", ADMIN_UID)
                                startActivity(intent)
                            }
                        }
                    true
                }
                R.id.nav_home -> {
                    true
                }
                R.id.nav_post -> {
                    val intent = Intent(this, PostListActivity::class.java)
                    startActivity(intent)

                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        whistlePlayer.release()
    }
}

