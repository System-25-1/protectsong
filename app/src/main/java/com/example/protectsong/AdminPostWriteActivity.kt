package com.example.protectsong

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityAdminPostWriteBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AdminPostWriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPostWriteBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPostWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isEdit = intent.getBooleanExtra("editMode", false)
        val postId = intent.getStringExtra("postId")

        val categories = listOf("공지", "일반")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinnerCategory.adapter = adapter

        // ✅ 수정 모드이면 기존 데이터 채워넣기
        if (isEdit) {
            title = "게시글 수정"
            binding.etTitle.setText(intent.getStringExtra("title"))
            binding.etContent.setText(intent.getStringExtra("content"))
            val category = intent.getStringExtra("category") ?: "공지"
            val index = categories.indexOf(category)
            binding.spinnerCategory.setSelection(index)

            // 버튼 텍스트도 수정
            binding.btnSubmit.text = "수정하기"
        }

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem.toString()

            if (title.isBlank() || content.isBlank()) {
                Toast.makeText(this, "제목과 내용을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val post = hashMapOf(
                "title" to title,
                "content" to content,
                "category" to category,
                "timestamp" to FieldValue.serverTimestamp()
            )

            if (isEdit && postId != null) {
                // ✅ 수정 모드: Firestore 업데이트
                db.collection("posts").document(postId)
                    .update(post as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "게시글이 수정되었습니다", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "수정 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // ✅ 등록 모드: 새 글 등록
                db.collection("posts").add(post)
                    .addOnSuccessListener {
                        Toast.makeText(this, "게시글이 등록되었습니다", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "등록 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}

