package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 🔙 뒤로가기
        binding.backButton.setOnClickListener {
            finish()
        }

        // ✅ 로그인 버튼 클릭
        binding.loginSubmitButton.setOnClickListener {
            val studentId = binding.studentIdEdit.text.toString().trim()
            val password = binding.passwordEdit.text.toString().trim()

            if (studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "학번과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firestore에서 학번으로 이메일 조회
            firestore.collection("users")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val email = result.documents[0].getString("email") ?: run {
                            Toast.makeText(this, "이메일 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        // 이메일 + 비밀번호로 로그인 시도

                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()

                                val uid = auth.currentUser?.uid ?: return@addOnSuccessListener
                                firestore.collection("users").document(uid)
                                    .get()
                                    .addOnSuccessListener { doc ->
                                        val role = doc.getString("role")
// ✅ 변경 후 (권장 방식)
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()

                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "권한 확인 실패", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            .addOnFailureListener {
                                Toast.makeText(this, "비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "해당 학번을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "로그인 중 오류 발생: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
