package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityAdminPostWriteBinding
import com.google.firebase.Timestamp
import java.util.UUID

class PostWriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPostWriteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPostWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem.toString()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 임시 Post 객체 생성
            val post = Post(
                id = UUID.randomUUID().toString(),
                title = title,
                content = content,
                category = category,
                timestamp = Timestamp.now()
            )

            // 임시 저장 (Intent로 객체 넘기기)
            val intent = Intent()
            intent.putExtra("newPost", post)
            setResult(RESULT_OK, intent)

            Toast.makeText(this, "등록 완료 (로컬)", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 하단 네비게이션 그대로 유지
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
                else -> false
            }
        }
    }
}
