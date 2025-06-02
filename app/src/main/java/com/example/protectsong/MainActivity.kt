package com.example.protectsong

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.accessibility.AccessibilityManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.protectsong.databinding.ActivityMainBinding
import com.example.protectsong.whistle.WhistleService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.io.File
import java.util.Locale
import android.graphics.Color

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var isWhistleOn = false
    private lateinit var whistlePlayer: MediaPlayer


    private val ADMIN_UID = "Os1oJCzG45OKwyglRdc0JXxbghw2"
    private val REQUEST_CALL_PERMISSION = 100
    private val REQUEST_CALL_PERMISSION_EMERGENCY = 100
    private val REQUEST_CALL_PERMISSION_SUPPORT = 101


    private lateinit var recorder: MediaRecorder
    private lateinit var tempFile: File
    private val soundHandler = Handler(Looper.getMainLooper())
    private var isLoudSoundDetected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 접근성 권한
        if (!isAccessibilityServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "‘지키송 휘슬 서비스’를 활성화해주세요", Toast.LENGTH_LONG).show()
        }

        // 툴바 설정
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, android.R.color.white)

        // 네비게이션 헤더
        val header = binding.navView.getHeaderView(0)
        val profileImageView = header.findViewById<ImageView>(R.id.navProfileImage)
        header.findViewById<TextView>(R.id.tvSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        header.findViewById<TextView>(R.id.tvMyReport).setOnClickListener {
            startActivity(Intent(this, MyReportActivity::class.java))
        }
        header.findViewById<TextView>(R.id.logout_button).setOnClickListener { logout() }
        header.findViewById<TextView>(R.id.tvMyProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // 사용자 정보
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                header.findViewById<TextView>(R.id.tvUserName).text = doc.getString("name") ?: "이름없음"
                header.findViewById<TextView>(R.id.tvStudentId).text = doc.getString("studentId") ?: "학번없음"
                val imageUrl = doc.getString("profileImageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this).load(imageUrl).circleCrop().into(profileImageView)
                }
            }

            FirebaseMessaging.getInstance().token.addOnCompleteListener { t ->
                if (t.isSuccessful) {
                    db.collection("users").document(uid).update("fcmToken", t.result)
                }
            }
        }

        // 네비게이션 메뉴
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_mypage -> Toast.makeText(this, "마이페이지 클릭됨", Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_logout -> logout()
                R.id.nav_my_report -> startActivity(Intent(this, MyReportActivity::class.java))
            }
            binding.drawerLayout.closeDrawers()
            true
        }

        // 문자 신고 버튼
        binding.btnSmsReport.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundResource(R.drawable.bg_left_curve_button_pressed)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundResource(R.drawable.bg_left_curve_button)
            }
            false
        }
        binding.btnSmsReport.setOnClickListener {
            startActivity(Intent(this, SmsReportActivity::class.java))
        }

        // 긴급 신고 버튼
        binding.btnEmergency.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundResource(R.drawable.bg_circle_button_pressed)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundResource(R.drawable.bg_circle_button)
            }
            false
        }
        binding.btnEmergency.setOnClickListener {
            makeEmergencyCall()
        }

        // 전화 신고 버튼
        binding.ivCall.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundResource(R.drawable.bg_right_curve_button_pressed)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.setBackgroundResource(R.drawable.bg_right_curve_button)
            }
            false
        }
        binding.ivCall.setOnClickListener {
            makeDirectCallToSupport()
        }

        // 휘슬 버튼
        binding.btnWhistle.setOnClickListener {
            isWhistleOn = !isWhistleOn
            binding.btnWhistle.setBackgroundResource(
                if (isWhistleOn) R.drawable.bg_rectangle_button_pressed
                else R.drawable.bg_rectangle_button
            )
            binding.btnWhistle.setImageResource(
                if (isWhistleOn) R.drawable.on else R.drawable.off
            )
        }

        // 하단 네비게이션
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> navigateToChat()
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

    private fun navigateToChat(): Boolean {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .get().addOnSuccessListener { doc ->
                    val role = doc.getString("role")
                    if (role == "admin") {
                        startActivity(Intent(this, ChatListActivity::class.java))
                    } else {
                        Intent(this, ChatActivity::class.java).apply {
                            putExtra("chatWithUserId", ADMIN_UID)
                            startActivity(this)
                        }
                    }
                }
        }
        return true
    }

    private fun logout() {
        soundHandler.removeCallbacksAndMessages(null)
        if (::recorder.isInitialized) recorder.stop().also { recorder.release() }
        FirebaseAuth.getInstance().signOut()
        Intent(this, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
        finishAffinity()
    }

    private fun requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 102)
        }
    }

    private fun startLoudSoundMonitor() {
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
                val amp = recorder.maxAmplitude
                if (amp > 50000 && !isLoudSoundDetected) {
                    isLoudSoundDetected = true
                    Toast.makeText(this@MainActivity, "큰 소리 감지됨. 신고 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
                    makeEmergencyCall()
                }
                soundHandler.postDelayed(this, 500)
            }
        })
    }

    private fun makeEmergencyCall() {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:112")
        }
        startActivity(dialIntent)
    }

    private fun makeDirectCallToSupport() {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:01089750220")
        }
        startActivity(dialIntent)
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                REQUEST_CALL_PERMISSION_EMERGENCY -> makeEmergencyCall()
                REQUEST_CALL_PERMISSION_SUPPORT -> makeDirectCallToSupport()
            }
        } else {
            Toast.makeText(this, "전화 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
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

                docs.forEach { doc ->
                    val title = doc.getString("title") ?: "제목 없음"
                    val timestamp = doc.getTimestamp("timestamp")
                    val dateStr = timestamp?.toDate()?.let {
                        java.text.SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(it)
                    } ?: ""

                    val rowLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        setPadding(16, 12, 16, 12)
                    }

                    val titleView = TextView(this).apply {
                        text = "• $title"
                        textSize = 20f
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }

                    val dateView = TextView(this).apply {
                        text = dateStr
                        textSize = 18f
                        setTextColor(Color.GRAY)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    rowLayout.setOnClickListener {
                        Intent(this@MainActivity, PostDetailActivity::class.java).apply {
                            putExtra("postId", doc.id)
                            startActivity(this)
                        }
                    }

                    rowLayout.addView(titleView)
                    rowLayout.addView(dateView)
                    container.addView(rowLayout)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "공지 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?: return false
        return enabled.split(":").any { it.contains(packageName) }
    }
}

