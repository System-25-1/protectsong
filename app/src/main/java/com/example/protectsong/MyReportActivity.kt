package com.example.protectsong

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityMyReportBinding

class MyReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 뒤로가기 버튼 (커스텀)
        binding.backText.setOnClickListener {
            finish()
        }

        // ✅ 검색 버튼
        binding.btnSearch.setOnClickListener {
            val date = binding.etDate.text.toString()
            val reportNumber = binding.etReportNumber.text.toString()

            if (date.isBlank() || reportNumber.isBlank()) {
                Toast.makeText(this, "날짜와 신고번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 더미 데이터로 예시 출력
            if (date == "2025/05/19" && reportNumber == "12345") {
                Toast.makeText(this, "신고 내용이 조회되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "해당 신고를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
