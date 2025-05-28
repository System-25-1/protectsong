package com.example.protectsong

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.protectsong.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var isWhistleOn = false
    private lateinit var whistlePlayer: MediaPlayer
    private val ADMIN_UID = "MecPxatzCTMeHztzELY4ps4KVeh2"
    private val REQUEST_CALL_PERMISSION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 툴바 설정
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

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
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, android.R.color.white)

        // ✅ 드로어 헤더 정보 설정
        val headerView = binding.navView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvStudentId = headerView.findViewById<TextView>(R.id.tvStudentId)
        val tvMyProfile = headerView.findViewById<TextView>(R.id.tvMyProfile)
        val logoutButton = headerView.findViewById<TextView>(R.id.logout_button)
        val tvSettings = headerView.findViewById<TextView>(R.id.tvSettings)
        val tvMyReport = headerView.findViewById<TextView>(R.id.tvMyReport)

        tvSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        tvMyReport.setOnClickListener {
            startActivity(Intent(this, MyReportActivity::class.java))
        }

        // ✅ Firebase 사용자 정보 로드 및 FCM 저장
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(uid)
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

            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        db.collection("users").document(uid).update("fcmToken", token)
                    }
                }
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        tvMyProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_mypage -> {
                    Toast.makeText(this, "마이페이지 클릭됨", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, SplashActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                R.id.nav_my_report -> {
                    val intent = Intent(this, MyReportActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // 버튼들
        binding.btnSmsReport.setOnClickListener {
            val intent = Intent(this, SmsReportActivity::class.java)
            startActivity(intent)
        }

        binding.btnEmergency.setOnClickListener {
            makeEmergencyCall()
        }

        whistlePlayer = MediaPlayer.create(this, R.raw.whistle_sound)
        binding.btnWhistle.setOnClickListener {
            isWhistleOn = !isWhistleOn
            binding.tvWhistle.text = if (isWhistleOn) "on" else "off"
            val backgroundRes = if (isWhistleOn)
                R.drawable.bg_rectangle_button_pressed
            else
                R.drawable.bg_rectangle_button
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

        binding.ivCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:010-8975-0220")
            startActivity(intent)
        }

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
                                startActivity(Intent(this, ChatListActivity::class.java))
                            } else {
                                val intent = Intent(this, ChatActivity::class.java)
                                intent.putExtra("chatWithUserId", ADMIN_UID)
                                startActivity(intent)
                            }
                        }
                    true
                }
                R.id.nav_home -> true
                R.id.nav_post -> {
                    val intent = Intent(this, PostListActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    // ✅ 반드시 onCreate 밖에 위치해야 함
    private fun makeEmergencyCall() {
        val phoneNumber = "tel:112"
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse(phoneNumber))
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_CALL_PERMISSION
            )
        } else {
            startActivity(callIntent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeEmergencyCall()
            } else {
                Toast.makeText(this, "전화 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        whistlePlayer.release()
    }
}
