package com.example.protectsong

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityAdminPostWriteBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AdminPostWriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPostWriteBinding
    private val db = FirebaseFirestore.getInstance()
    private var editMode = false
    private var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPostWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 카테고리 Spinner 설정
        val categories = listOf("공지", "일반")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinnerCategory.adapter = adapter

        // ✅ 수정 모드인지 확인
        editMode = intent.getBooleanExtra("editMode", false)

        if (editMode) {
            // 수정 대상 데이터 설정
            postId = intent.getStringExtra("postId")
            val title = intent.getStringExtra("title") ?: ""
            val content = intent.getStringExtra("content") ?: ""
            val category = intent.getStringExtra("category") ?: "공지"

            binding.etTitle.setText(title)
            binding.etContent.setText(content)
            val index = categories.indexOf(category)
            if (index >= 0) binding.spinnerCategory.setSelection(index)
        }

        // ✅ 등록 버튼 클릭
        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem.toString()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (editMode) {
                // ✏ 기존 문서 수정
                val data = mapOf(
                    "title" to title,
                    "content" to content,
                    "category" to category
                )

                db.collection("posts").document(postId!!)
                    .update(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, "수정 완료", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "수정 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // ➕ 새 글 등록
                val newId = db.collection("posts").document().id
                val post = Post(
                    id = newId,
                    title = title,
                    content = content,
                    category = category,
                    timestamp = Timestamp.now()
                )

                db.collection("posts").document(newId)
                    .set(post)
                    .addOnSuccessListener {
                        Toast.makeText(this, "게시글 등록 완료", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "등록 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
