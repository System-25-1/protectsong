package com.example.protectsong

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityEditProfileBinding

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

        // ✅ 수정 버튼 클릭
        binding.btnUpdate.setOnClickListener {
            val guardianName = binding.editGuardianName.text.toString()
            val guardianPhone = binding.editGuardianPhone.text.toString()
            val guardianRelation = binding.spinnerRelation.selectedItem.toString()

            // 📦 SharedPreferences에 저장
            val prefs = getSharedPreferences("guardian_info", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString("guardian_name", guardianName)
                putString("guardian_phone", guardianPhone)
                putString("guardian_relation", guardianRelation)
                apply()
            }

            // ✅ 완료 메시지 표시
            Toast.makeText(this, "수정되었습니다", Toast.LENGTH_SHORT).show()
        }
    }
}
