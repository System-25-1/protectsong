package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        // 🔙 커스텀 툴바 '뒤로' 텍스트 클릭 시 -> AdminMainActivity로 이동
        binding.toolbar.backText.setOnClickListener {
            startActivity(Intent(this, AdminMainActivity::class.java))
            finish()
        }

        // 🔄 인텐트에서 report 데이터 받기
        report = intent.getParcelableExtra("report") ?: return finish()

        // 📄 데이터 표시
        binding.tvCategory.text = report.type
        binding.tvBuilding.text = report.building
        binding.tvContent.text = report.content

        // 🌀 상태 Spinner 세팅
        val statusList = listOf("접수됨", "처리중", "처리완료")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusList)
        binding.spinnerStatus.adapter = adapter
        binding.spinnerStatus.setSelection(statusList.indexOf(report.status))

        // 💾 저장 버튼 클릭 시 Firestore 업데이트
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

        // ⬇️ BottomNavigationView 이벤트 처리
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatListActivity::class.java))
                    true
                }
                R.id.nav_home -> true // 현재 화면 유지
                R.id.nav_post -> {
                    startActivity(Intent(this, PostListActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
