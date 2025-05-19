package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityPostDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val postId = intent.getStringExtra("postId") ?: return

        db.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    val category = document.getString("category") ?: ""

                    binding.tvTitle.text = title
                    binding.tvContent.text = content
                    binding.tvCategory.text = category

                    // ✅ 관리자만 수정/삭제 버튼 보이게
                    if (userIsAdmin()) {
                        binding.btnEdit.visibility = View.VISIBLE
                        binding.btnDelete.visibility = View.VISIBLE
                    }

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

                    binding.btnDelete.setOnClickListener {
                        db.collection("posts").document(postId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "삭제 완료", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "삭제 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
    }

    private fun userIsAdmin(): Boolean {
        val adminUid = "YOUR_ADMIN_UID" // 🔁 여기에 관리자 UID 입력
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        return currentUid == adminUid
    }
}
