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
import com.google.firebase.firestore.Query

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

        binding.backText.setOnClickListener {
            finish()
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔽 최신순 정렬된 문자 신고 조회
        firestore.collection("smsReports")
            .whereEqualTo("userId", uid)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val reports = documents.mapNotNull { doc ->
                    Report(
                        id = doc.id,
                        date = doc.getTimestamp("date")?.toDate(),
                        content = doc.getString("content") ?: "",
                        building = doc.getString("building") ?: "",
                        status = doc.getString("status") ?: "접수됨"
                    )
                }
                adapter.submitList(reports)
                if (reports.isEmpty()) {
                    Toast.makeText(this, "신고 내역이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "오류 발생: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
