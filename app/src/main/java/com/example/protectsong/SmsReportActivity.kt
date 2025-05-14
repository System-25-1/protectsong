package com.example.protectsong

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivitySmsReportBinding

class SmsReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySmsReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmsReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }
}
