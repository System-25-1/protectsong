// 생략된 import는 기존 그대로 유지
package com.example.protectsong

import com.example.protectsong.accessibility.UnifiedAccessibilityService
import android.content.ComponentName
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val ADMIN_UID = "Os1oJCzG45OKwyglRdc0JXxbghw2"
    private val REQUEST_CALL_PHONE = 103
    private val REQUEST_RECORD_AUDIO = 104

    private lateinit var recorder: MediaRecorder
    private lateinit var tempFile: File
    private val soundHandler = Handler(Looper.getMainLooper())
    private var loudStartTime: Long? = null
    private var isLoudSoundDetected = false

    private var isWhistlePlaying = false
    private var whistlePlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkCallPermission()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!isAccessibilityServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "‘지키송 접근성 서비스’를 활성화해주세요", Toast.LENGTH_LONG).show()
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, android.R.color.white)

        // ✅ 햄버거 아이콘 흰색으로 설정
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.white)

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

        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_logout -> logout()
                R.id.nav_my_report -> startActivity(Intent(this, MyReportActivity::class.java))
            }
            binding.drawerLayout.closeDrawers()
            true
        }

        binding.btnSmsReport.setOnClickListener {
            binding.btnSmsReport.setBackgroundResource(R.drawable.bg_left_curve_button_pressed)
            startActivity(Intent(this, SmsReportActivity::class.java))
            binding.btnSmsReport.postDelayed({
                binding.btnSmsReport.setBackgroundResource(R.drawable.bg_left_curve_button)
            }, 200)
        }

        binding.btnEmergency.setOnClickListener {
            binding.btnEmergency.setBackgroundResource(R.drawable.bg_circle_button_pressed)
            makeEmergencyCall()
            binding.btnEmergency.postDelayed({
                binding.btnEmergency.setBackgroundResource(R.drawable.bg_circle_button)
            }, 200)
        }

        binding.ivCall.setOnClickListener {
            binding.ivCall.setBackgroundResource(R.drawable.bg_right_curve_button_pressed)
            binding.ivCall.postDelayed({
                binding.ivCall.setBackgroundResource(R.drawable.bg_right_curve_button)
            }, 200)
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:01089750220")
            }
            startActivity(dialIntent)
        }

        binding.btnWhistle.setImageResource(R.drawable.off)
        binding.btnWhistle.setOnClickListener {
            toggleWhistle()
        }

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

        checkAndStartAudioMonitor() // 🔄 변경된 부분
    }

    private fun checkAndStartAudioMonitor() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO)
        } else {
            startLoudSoundMonitor()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CALL_PHONE && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            callEmergencyNumberDirectly()
        }

        if (requestCode == REQUEST_RECORD_AUDIO && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLoudSoundMonitor()
        } else if (requestCode == REQUEST_RECORD_AUDIO) {
            Toast.makeText(this, "마이크 권한이 필요합니다", Toast.LENGTH_SHORT).show()
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
                    val amp = recorder.maxAmplitude
                    Log.d("AMP", "현재 amp: $amp")
                    val now = System.currentTimeMillis()

                    if (amp > 25000) {
                        if (loudStartTime == null) loudStartTime = now
                        if (now - loudStartTime!! >= 2000 && !isLoudSoundDetected) {
                            isLoudSoundDetected = true
                            Toast.makeText(this@MainActivity, "2초 이상 고함 감지! 신고 화면 이동", Toast.LENGTH_SHORT).show()
                            makeEmergencyCall()
                        }
                    } else {
                        loudStartTime = null
                    }

                    soundHandler.postDelayed(this, 500)
                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, "마이크 사용 실패: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun checkCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                AlertDialog.Builder(this)
                    .setTitle("전화 권한 필요")
                    .setMessage("신고 기능을 사용하려면 전화 권한이 필요합니다.\n설정으로 이동하여 권한을 허용해 주세요.")
                    .setPositiveButton("설정") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                    .setNegativeButton("취소", null)
                    .show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
            }
        }
    }

    private fun toggleWhistle() {
        if (!isWhistlePlaying) {
            whistlePlayer = MediaPlayer.create(this, R.raw.whistle_sound)
            whistlePlayer?.isLooping = true
            whistlePlayer?.start()

            binding.btnWhistle.setImageResource(R.drawable.on)
            binding.btnWhistle.setBackgroundResource(R.drawable.bg_rectangle_button_pressed)
            isWhistlePlaying = true
        } else {
            whistlePlayer?.stop()
            whistlePlayer?.release()
            whistlePlayer = null

            binding.btnWhistle.setImageResource(R.drawable.off)
            binding.btnWhistle.setBackgroundResource(R.drawable.bg_rectangle_button)
            isWhistlePlaying = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isWhistlePlaying) {
            whistlePlayer?.stop()
            whistlePlayer?.release()
        }
    }

    private fun makeEmergencyCall() {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:112")
        }
        startActivity(dialIntent)
    }

    private fun callEmergencyNumberDirectly() {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:01093808120")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
            return
        }
        startActivity(callIntent)
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

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val myService = ComponentName(this, UnifiedAccessibilityService::class.java)
        val expected = myService.flattenToString()
        return enabledServices.split(":").any { it == expected }
    }

    override fun onResume() {
        super.onResume()
        loadNotices()
        loadUserInfoToNavHeader()
    }
    private fun loadUserInfoToNavHeader() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val header = binding.navView.getHeaderView(0)
        val profileImageView = header.findViewById<ImageView>(R.id.navProfileImage)
        val userNameView = header.findViewById<TextView>(R.id.tvUserName)
        val studentIdView = header.findViewById<TextView>(R.id.tvStudentId)

        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            userNameView.text = doc.getString("name") ?: "이름없음"
            studentIdView.text = doc.getString("studentId") ?: "학번없음"
            val imageUrl = doc.getString("profileImageUrl")
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.default_profile) // 기본 이미지
            }
        }
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
                        SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(it)
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
                        textSize = 18f
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }

                    val dateView = TextView(this).apply {
                        text = dateStr
                        textSize = 15f
                        setTextColor(android.graphics.Color.GRAY)
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
}
