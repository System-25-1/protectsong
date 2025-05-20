package com.example.protectsong

<<<<<<< HEAD
import android.content.Intent
=======
>>>>>>> feature/eunseo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityAdminReportListBinding
<<<<<<< HEAD
=======
import com.example.protectsong.model.Report
import com.example.protectsong.adapter.AdminReportAdapter
import com.google.firebase.auth.FirebaseAuth
>>>>>>> feature/eunseo
import com.google.firebase.firestore.FirebaseFirestore

class AdminReportListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminReportListBinding
<<<<<<< HEAD
    private lateinit var adapter: ReportAdapter
    private val reports = mutableListOf<Report>()
=======
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: AdminReportAdapter
>>>>>>> feature/eunseo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminReportListBinding.inflate(layoutInflater)
        setContentView(binding.root)

<<<<<<< HEAD
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
=======
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // 관리자 권한 확인
        firestore.collection("users").document(auth.currentUser?.uid ?: "").get()
            .addOnSuccessListener { document ->
                val isAdmin = document.getBoolean("isAdmin") ?: false
                if (!isAdmin) {
                    Toast.makeText(this, "접근 권한이 없습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    setupRecyclerView()
                    loadReports()
                }
            }
    }

    private fun setupRecyclerView() {
        adapter = AdminReportAdapter { report, newStatus ->
            firestore.collection("reports").document(report.id)
                .update("status", newStatus)
        }

        binding.recyclerViewAdminReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAdminReports.adapter = adapter
    }

    private fun loadReports() {
        firestore.collection("reports")
            .get()
            .addOnSuccessListener { documents ->
                val reports = documents.mapNotNull { it.toObject(Report::class.java).copy(id = it.id) }
                adapter.submitList(reports)
            }
            .addOnFailureListener {
                Toast.makeText(this, "불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
>>>>>>> feature/eunseo
            }
    }
}
