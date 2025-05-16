package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityAdminPostWriteBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AdminPostWriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminPostWriteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPostWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Spinner 설정
        val categories = listOf("공지", "일반")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinnerCategory.adapter = adapter

        // 등록 버튼 클릭
        binding.btnSubmit.setOnClickListener {
            val category = binding.spinnerCategory.selectedItem.toString()
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()

            if (title.isBlank() || content.isBlank()) {
                Toast.makeText(this, "제목과 내용을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firestore에 업로드할 데이터 구성
            val post = hashMapOf(
                "title" to title,
                "content" to content,
                "category" to category,
                "timestamp" to FieldValue.serverTimestamp()
            )

            // Firestore 업로드
            FirebaseFirestore.getInstance().collection("posts")
                .add(post)
                .addOnSuccessListener {
                    Toast.makeText(this, "게시글이 등록되었습니다", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "업로드 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // 하단 바 설정
        binding.bottomNavigation.selectedItemId = R.id.nav_post
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    true
                }
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_post -> true
                else -> false
            }
        }
    }
}
