package com.example.protectsong

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityAdminPostWriteBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent

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

        // ✅ 수정 모드 여부 확인
        editMode = intent.getBooleanExtra("editMode", false)

        if (editMode) {
            // ✅ 기존 글 정보 불러오기
            postId = intent.getStringExtra("postId")
            val title = intent.getStringExtra("title") ?: ""
            val content = intent.getStringExtra("content") ?: ""
            val category = intent.getStringExtra("category") ?: "공지"

            binding.etTitle.setText(title)
            binding.etContent.setText(content)
            val index = categories.indexOf(category)
            if (index >= 0) binding.spinnerCategory.setSelection(index)
        }

        // ✅ 등록 버튼 클릭 처리
        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem.toString()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔒 글자수 제한 검사
            if (title.length > 30) {
                Toast.makeText(this, "제목은 30자 이내로 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (content.length > 5000) {
                Toast.makeText(this, "내용은 5000자 이내로 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (editMode) {
                // ✏ 기존 게시글 수정
                val data = mapOf(
                    "title" to title,
                    "content" to content,
                    "category" to category
                )

                db.collection("posts").document(postId!!)
                    .update(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, "수정 완료", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)  // ✅ 수정 결과 전달
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
                        setResult(RESULT_OK)  // ✅ 등록 결과 전달
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "등록 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // ✅ 하단 네비게이션 클릭 리스너 설정
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatListActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_home -> {
                    startActivity(Intent(this, AdminMainActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_post -> {
                    true
                }
                else -> false
            }
        }

        // ✅ 현재 탭 강조
        binding.bottomNavigation.post {
            binding.bottomNavigation.selectedItemId = R.id.nav_post
        }
    }
}
