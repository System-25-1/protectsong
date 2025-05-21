package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityPostDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val postId = intent.getStringExtra("postId") ?: return

        // ✅ 게시글 데이터 가져오기
        db.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    val category = document.getString("category") ?: ""

                    binding.tvTitle.text = title
                    binding.tvContent.text = content
                    binding.tvCategory.text = category

                    // ✅ 로그인 사용자 role 확인
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        FirebaseFirestore.getInstance().collection("users").document(uid)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                val role = userDoc.getString("role")
                                if (role == "admin") {
                                    binding.btnEdit.visibility = View.VISIBLE
                                    binding.btnDelete.visibility = View.VISIBLE

                                    // ✏ 수정 버튼
                                    binding.btnEdit.setOnClickListener {
                                        val intent = Intent(this, AdminPostWriteActivity::class.java).apply {
                                            putExtra("editMode", true)
                                            putExtra("postId", postId)
                                            putExtra("title", title)
                                            putExtra("content", content)
                                            putExtra("category", category)
                                        }
                                        startActivity(intent)
                                        finish()
                                    }

                                    // 🗑 삭제 버튼
                                    binding.btnDelete.setOnClickListener {
                                        db.collection("posts").document(postId)
                                            .delete()
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "삭제 완료", Toast.LENGTH_SHORT).show()

                                                // ✅ Main으로 돌아가면서 목록 새로고침 요청
                                                val intent = Intent()
                                                intent.putExtra("postDeleted", true)
                                                setResult(RESULT_OK, intent)
                                                finish()
                                            }

                                    }
                                }
                            }
                    }
                } else {
                    Toast.makeText(this, "게시글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                finish()
            }

        // 📦 목록으로 돌아가기
        binding.btnClose.setOnClickListener {
            finish()
        }
    }
}
