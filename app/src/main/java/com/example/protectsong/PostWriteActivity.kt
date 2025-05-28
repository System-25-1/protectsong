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

        // ✅ 하단 네비게이션 클릭 리스너 설정
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatListActivity::class.java))  // 관리자 채팅 목록 화면
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_home -> {
                    startActivity(Intent(this, AdminMainActivity::class.java))  // 관리자 메인 화면
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_post -> {
                    // 현재 화면 → 아무 동작 없음
                    true
                }
                else -> false
            }
        }

        // ✅ 현재 탭을 Post로 설정해서 강조
        binding.bottomNavigation.selectedItemId = R.id.nav_post

        // ✅ 글 등록 버튼 클릭 리스너
        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem.toString()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 새 Post 객체 생성
            val post = Post(
                id = UUID.randomUUID().toString(),
                title = title,
                content = content,
                category = category,
                timestamp = Timestamp.now()
            )

            // 임시 저장 (Activity 간 데이터 전달용)
            val intent = Intent()
            intent.putExtra("newPost", post)
            setResult(RESULT_OK, intent)

            Toast.makeText(this, "등록 완료 (로컬)", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
