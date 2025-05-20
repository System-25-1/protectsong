package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityAdminReportListBinding
import com.google.firebase.firestore.FirebaseFirestore

class AdminReportListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminReportListBinding
    private lateinit var adapter: ReportAdapter
    private val reports = mutableListOf<Report>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminReportListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ReportAdapter(reports) { report ->
            Toast.makeText(this@AdminReportListActivity, "${report.type} 신고 선택됨", Toast.LENGTH_SHORT).show()
            // TODO: 상태 변경 다이얼로그 호출 등 추가 작업
        }

        binding.recyclerViewReports.adapter = adapter
        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this@AdminReportListActivity)

        FirebaseFirestore.getInstance().collection("smsReports")
            .get()
            .addOnSuccessListener { documents ->
                reports.clear()
                for (doc in documents) {
                    val report = Report(
                        id = doc.id,
                        building = doc.getString("building") ?: "",
                        content = doc.getString("content") ?: "",
                        type = doc.getString("type") ?: "",
                        timestamp = doc.getTimestamp("timestamp")?.toDate()?.toString() ?: "",
                        uid = doc.getString("uid") ?: ""
                    )
                    reports.add(report)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this@AdminReportListActivity, "불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
