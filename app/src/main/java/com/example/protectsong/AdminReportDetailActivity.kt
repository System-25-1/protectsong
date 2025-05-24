package com.example.protectsong

import android.os.Bundle
import android.widget.ArrayAdapter
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

        report = intent.getParcelableExtra("report") ?: return finish()

        binding.tvCategory.text = report.type
        binding.tvBuilding.text = report.building
        binding.tvContent.text = report.content

        val statusList = listOf("접수됨", "처리중", "처리완료")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusList)
        binding.spinnerStatus.adapter = adapter
        binding.spinnerStatus.setSelection(statusList.indexOf(report.status))

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
    }
}
