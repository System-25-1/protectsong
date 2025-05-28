package com.example.protectsong

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.adapter.ReportAdapter
import com.example.protectsong.databinding.ActivityMyReportBinding
import com.example.protectsong.model.Report
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


        // ✅ 뒤로가기
        binding.backText.setOnClickListener {
            finish()
        }

        // ✅ 검색 버튼
        binding.btnSearch.setOnClickListener {
            val queryText = binding.etContent.text.toString().trim()

            if (queryText.isEmpty()) {
                loadMyReports()
                return@setOnClickListener
            }

            val uid = auth.currentUser?.uid ?: return@setOnClickListener

            firestore.collection("smsReports")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener { result ->
                    val matchedReports = result.documents.mapNotNull { doc ->
                        val report = doc.toObject(Report::class.java)?.apply { id = doc.id }
                        if (report?.content?.contains(queryText, ignoreCase = true) == true) report else null
                    }.sortedByDescending { it.timestamp }

                    if (matchedReports.isEmpty()) {
                        Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                    }

                    adapter.submitList(matchedReports)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "검색에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        }

        // ✅ 전체 신고 내역 불러오기
        loadMyReports()
    }

    private fun loadMyReports() {
        val uid = auth.currentUser?.uid ?: return
        Log.d("MyReportActivity", "현재 로그인 UID: $uid")

        firestore.collection("smsReports")
            .whereEqualTo("uid", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val reports: List<Report> = documents.mapNotNull { doc ->
                    doc.toObject(Report::class.java)?.also { it.id = doc.id }
                }

                Log.d("MyReportActivity", "불러온 문서 수: ${reports.size}")

                if (reports.isEmpty()) {
                    Toast.makeText(this, "조회된 신고 내역이 없습니다.", Toast.LENGTH_SHORT).show()
                }

                adapter.submitList(reports)
            }
            .addOnFailureListener { e ->
                Log.e("MyReportActivity", "쿼리 실패: ${e.message}", e)
                Toast.makeText(this, "신고 조회에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
    }

}
