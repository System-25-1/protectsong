package com.example.protectsong

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityMyReportBinding
<<<<<<< HEAD
=======
import com.example.protectsong.model.Report
import com.example.protectsong.adapter.ReportAdapter
import com.google.firebase.auth.FirebaseAuth
>>>>>>> feature/eunseo
import com.google.firebase.firestore.FirebaseFirestore

class MyReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyReportBinding
<<<<<<< HEAD
    private val db = FirebaseFirestore.getInstance()
=======
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ReportAdapter
>>>>>>> feature/eunseo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

<<<<<<< HEAD
        // 🔙 뒤로가기 버튼
        binding.backText.setOnClickListener {
            finish()
        }

        // 🔍 검색 버튼 클릭
=======
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        adapter = ReportAdapter()
        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReports.adapter = adapter

>>>>>>> feature/eunseo
        binding.btnSearch.setOnClickListener {
            val date = binding.etDate.text.toString().trim()
            val number = binding.etReportNumber.text.toString().trim()
            val uid = auth.currentUser?.uid

            if (uid == null || date.isBlank() || number.isBlank()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

<<<<<<< HEAD
            // 🔥 Firestore에서 해당 날짜 + 신고번호로 검색
            db.collection("reports")
                .whereEqualTo("date", date)
                .whereEqualTo("reportNumber", reportNumber)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (doc in documents) {
                            val title = doc.getString("title") ?: "제목 없음"
                            val content = doc.getString("content") ?: "내용 없음"
                            val status = doc.getString("status") ?: "상태 없음"

                            Toast.makeText(
                                this,
                                "📄 제목: $title\n📌 상태: $status\n📝 내용: $content",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(this, "해당 신고를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "검색 중 오류 발생: ${it.message}", Toast.LENGTH_SHORT).show()
=======
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
>>>>>>> feature/eunseo
                }
        }
    }
}
