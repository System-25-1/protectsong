package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivitySmsReportBinding
import com.example.protectsong.MainActivity


class SmsReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySmsReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmsReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //뒤로가면 mainactivity
        binding.backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        // 신고 유형 스피너 설정
        val typeAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.report_types,
            android.R.layout.simple_spinner_item
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = typeAdapter

        // 강의실 스피너 설정
        // 건물명 Spinner 어댑터 연결
        val buildingAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.building_names,
            android.R.layout.simple_spinner_item
        )
        buildingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBuilding.adapter = buildingAdapter

        binding.btnSubmit.setOnClickListener {
            // 팝업 다이얼로그 생성
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("신고 완료!")
            builder.setMessage("신고가 성공적으로 접수되었습니다.")
            builder.setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
                // MainActivity로 이동
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish() // 현재 액티비티 종료
            }
            builder.show()
        }

    }
}
