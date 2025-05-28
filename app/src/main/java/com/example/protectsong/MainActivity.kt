package com.example.protectsong

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.util.Log
import android.widget.LinearLayout
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
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var isWhistleOn = false
    private lateinit var whistlePlayer: MediaPlayer
    private val ADMIN_UID = "MecPxatzCTMeHztzELY4ps4KVeh2"
    private val REQUEST_CALL_PERMISSION = 100

    private lateinit var recorder: MediaRecorder
    private lateinit var tempFile: File
    private val soundHandler = Handler(Looper.getMainLooper())
    private var isLoudSoundDetected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔽 접근성 권한이 꺼져 있다면 요청
        if (!isAccessibilityServiceEnabled()) {
            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "‘지키송 휘슬 서비스’를 활성화해주세요", Toast.LENGTH_LONG).show()
        }

        // ✅ 툴바 설정
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, android.R.color.white)

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
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        db.collection("users").document(uid).update("fcmToken", token)
                    }
                }
        }

        logoutButton.setOnClickListener {
            logout()
        }

        tvMyProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
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
                    logout()
                    true
                }
                R.id.nav_my_report -> {
                    startActivity(Intent(this, MyReportActivity::class.java))
                    true
                }
                else -> false
            }
        }

        binding.btnSmsReport.setOnClickListener {
            startActivity(Intent(this, SmsReportActivity::class.java))
        }

        binding.btnEmergency.setOnClickListener {
            makeEmergencyCall()
        }

        whistlePlayer = MediaPlayer.create(this, R.raw.whistle_sound)
        binding.btnWhistle.setOnClickListener {
            isWhistleOn = !isWhistleOn
            binding.tvWhistle.text = if (isWhistleOn) "on" else "off"
            val bg = if (isWhistleOn)
                R.drawable.bg_rectangle_button_pressed
            else
                R.drawable.bg_rectangle_button
            binding.btnWhistle.setBackgroundResource(bg)

            if (isWhistleOn) whistlePlayer.start()
            else {
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
                    startActivity(Intent(this, PostListActivity::class.java))
                    true
                }
                else -> false
            }
        }

        requestMicrophonePermission()
        startLoudSoundMonitor()
    }

    private fun logout() {
        // 🔇 소리 감지 중단
        soundHandler.removeCallbacksAndMessages(null)

        // 🎙 MediaRecorder 안전하게 정리
        if (::recorder.isInitialized) {
            try {
                recorder.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            recorder.release()
        }

        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)

        finishAffinity() // 모든 액티비티 종료
    }

    private fun requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                102
            )
        }
    }

    private fun startLoudSoundMonitor() {
        try {
            tempFile = File.createTempFile("temp_audio", ".3gp", cacheDir)

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(tempFile.absolutePath)

                prepare()
                start()
            }

            soundHandler.post(object : Runnable {
                override fun run() {
                    try {
                        if (::recorder.isInitialized) {
                            val amp = recorder.maxAmplitude
                            if (amp > 2000 && !isLoudSoundDetected) {
                                isLoudSoundDetected = true
                                Toast.makeText(this@MainActivity, "큰 소리 감지됨. 신고 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
                                makeEmergencyCall()
                            }
                        }
                        soundHandler.postDelayed(this, 500)
                    } catch (e: IllegalStateException) {
                        Log.e("SoundDetection", "Recorder released")
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("SoundDetection", "소리 감지 초기화 실패: ${e.message}")
            Toast.makeText(this, "소리 감지 초기화 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeEmergencyCall() {
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:112"))
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
        if (::recorder.isInitialized) {
            try {
                recorder.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            recorder.release()
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotices()
    }

    private fun loadNotices() {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
            .whereEqualTo("category", "공지")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { docs ->
                val container = findViewById<LinearLayout>(R.id.notice_container)
                if (container.childCount > 1) container.removeViews(1, container.childCount - 1)
                for (doc in docs) {
                    val postId = doc.id
                    val title = doc.getString("title") ?: "제목 없음"
                    val textView = TextView(this).apply {
                        text = "• $title"
                        textSize = 14f
                        setPadding(16, 16, 16, 16)
                        setTextColor(android.graphics.Color.BLACK)
                        setBackgroundColor(android.graphics.Color.parseColor("#EEEEEE"))
                        setOnClickListener {
                            val intent = Intent(this@MainActivity, PostDetailActivity::class.java)
                            intent.putExtra("postId", postId)
                            startActivity(intent)
                        }
                    }
                    container.addView(textView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "공지 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        val enabledServices = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val packageName = applicationContext.packageName
        return enabledServices.split(":").any { it.contains(packageName) }
    }

}
