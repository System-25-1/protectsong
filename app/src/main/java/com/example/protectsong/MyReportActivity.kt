package com.example.protectsong

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityMyReportBinding
import com.google.firebase.firestore.FirebaseFirestore

class MyReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyReportBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔙 뒤로가기 버튼
        binding.backText.setOnClickListener {
            finish()
        }

        // 🔍 검색 버튼 클릭
        binding.btnSearch.setOnClickListener {
            val date = binding.etDate.text.toString()
            val reportNumber = binding.etReportNumber.text.toString()

            if (date.isBlank() || reportNumber.isBlank()) {
                Toast.makeText(this, "날짜와 신고번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
                }
        }
    }
}
