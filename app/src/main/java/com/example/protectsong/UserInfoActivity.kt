package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityUserInfoBinding

class UserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //  여기서 Spinner 어댑터 연결
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.relationship_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.relationshipSpinner.adapter = adapter

        // 뒤로가기
        binding.backButton.setOnClickListener {
            finish()
        }

        // 저장 버튼
        binding.saveButton.setOnClickListener {
            // TODO: Firebase에 사용자 정보 저장 로직 넣을 위치

            // 로그인 화면으로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            // 현재 화면 종료
            finish()
        }
    }
}
