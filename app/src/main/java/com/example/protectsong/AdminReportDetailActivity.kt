package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.protectsong.databinding.ActivityAdminSmsDetailBinding
import com.example.protectsong.model.SmsReport
import com.google.firebase.firestore.FirebaseFirestore

class AdminReportDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminSmsDetailBinding
    private lateinit var report: SmsReport
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSmsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바 뒤로가기
        binding.toolbar.backText.setOnClickListener {
            startActivity(Intent(this, AdminMainActivity::class.java))
            finish()
        }

        // 인텐트 데이터 받기
        report = intent.getParcelableExtra("report") ?: return finish()

        // 신고 내용 표시
        binding.tvCategory.text = report.type
        binding.tvBuilding.text = report.building
        binding.tvContent.setText(report.content)

        // 신고자 정보 표시
        db.collection("users").document(report.userId)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "이름없음"
                val sid = doc.getString("studentId") ?: "학번없음"
                binding.tvReporter.text = "신고자  $name/$sid"
            }
            .addOnFailureListener {
                binding.tvReporter.text = "신고자 정보를 불러오지 못했습니다."
            }

        // 미디어 처리 (이미지 or 비디오)
        if (report.files.isNotEmpty()) {
            val url = report.files[0]
            val isVideo = url.contains(".mp4") || url.contains(".mov") || url.contains(".avi")

            binding.imageSection.visibility = View.VISIBLE
            Glide.with(this).load(url).thumbnail(0.1f).into(binding.imageAttachment)

            if (isVideo) {
                binding.playIconOverlay.visibility = View.VISIBLE
                binding.mediaContainer.setOnClickListener {
                    val intent = Intent(this, VideoPlayerActivity::class.java)
                    intent.putExtra("videoUrl", url)
                    startActivity(intent)
                }
            } else {
                binding.playIconOverlay.visibility = View.GONE
                binding.mediaContainer.setOnClickListener {
                    val dialog = ImageDialog.newInstance(url)
                    dialog.show(supportFragmentManager, "ImageDialog")
                }
            }
            // ✅ 이미지 또는 영상 클릭 시 전체 보기 처리
            binding.mediaContainer.setOnClickListener {
                if (isVideo) {
                    val intent = Intent(this, VideoPlayerActivity::class.java)
                    intent.putExtra("videoUrl", url)
                    startActivity(intent)
                } else {
                    val dialog = FullImageDialog.newInstance(url)
                    dialog.show(supportFragmentManager, "FullImageDialog")
                }
            }

        } else {
            binding.imageSection.visibility = View.GONE
        }

        // 상태 Spinner 설정
        val statusList = listOf("접수됨", "처리중", "처리완료")
        binding.spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusList)
        binding.spinnerStatus.setSelection(statusList.indexOf(report.status))

        // 저장 버튼
        binding.btnSave.setOnClickListener {
            val newStatus = binding.spinnerStatus.selectedItem.toString()
            db.collection("smsReports")
                .document(report.id)
                .update("status", newStatus)
                .addOnSuccessListener {
                    Toast.makeText(this, "상태가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "저장 실패", Toast.LENGTH_SHORT).show()
                }
        }

        // 하단 네비게이션
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatListActivity::class.java))
                    true
                }
                R.id.nav_home -> {
                    startActivity(Intent(this, AdminMainActivity::class.java))
                    true
                }
                R.id.nav_post -> {
                    startActivity(Intent(this, PostListActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
