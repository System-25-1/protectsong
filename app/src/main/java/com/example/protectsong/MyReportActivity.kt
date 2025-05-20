package com.example.protectsong

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityMyReportBinding
import com.example.protectsong.model.Report
import com.example.protectsong.adapter.ReportAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyReportBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        adapter = ReportAdapter()
        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReports.adapter = adapter

        binding.btnSearch.setOnClickListener {
            val date = binding.etDate.text.toString().trim()
            val number = binding.etReportNumber.text.toString().trim()
            val uid = auth.currentUser?.uid

            if (uid == null || date.isBlank() || number.isBlank()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firestore.collection("reports")
                .whereEqualTo("userId", uid)
                .whereEqualTo("date", date)
                .whereEqualTo("number", number)
                .get()
                .addOnSuccessListener { documents ->
                    val reports = documents.mapNotNull { it.toObject(Report::class.java) }
                    adapter.submitList(reports)
                    if (reports.isEmpty()) {
                        Toast.makeText(this, "조회된 신고가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "오류 발생: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
