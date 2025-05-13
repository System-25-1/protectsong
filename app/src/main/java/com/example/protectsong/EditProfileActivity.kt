package com.example.protectsong

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityEditProfileBinding
import android.widget.ArrayAdapter


class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔙 뒤로 버튼
        binding.backText.setOnClickListener {
            finish()
        }

        // 🔽 보호자 관계 스피너 설정
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.relationship_options, // values/strings.xml에 정의됨
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerRelation.adapter = adapter

        // 수정 버튼 클릭
        binding.btnUpdate.setOnClickListener {
            // TODO: 수정 처리 로직
        }
    }
}