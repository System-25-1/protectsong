package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.view.View
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

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.loginSubmitButton.setOnClickListener {
            val studentId = binding.studentIdEdit.text.toString().trim()
            val password = binding.passwordEdit.text.toString().trim()

            if (studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "학번과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fakeEmail = "$studentId@protectsong.app"

            // 로딩 UI 처리 (선택적으로 적용 가능)
            binding.loginSubmitButton.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(fakeEmail, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid ?: return@addOnSuccessListener

                    firestore.collection("users").document(uid)
                        .get()
                        .addOnSuccessListener { doc ->
                            binding.loginSubmitButton.isEnabled = true
                            binding.progressBar.visibility = View.GONE

                            if (!doc.exists()) {
                                Toast.makeText(this, "사용자 정보가 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }

                            val role = doc.getString("role")
                            if (role == null) {
                                Toast.makeText(this, "사용자 권한이 설정되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }

                            if (role == "admin") {
                                startActivity(Intent(this, AdminMainActivity::class.java))
                            } else {
                                startActivity(Intent(this, MainActivity::class.java))
                            }
                            finish()
                        }
                        .addOnFailureListener {
                            binding.loginSubmitButton.isEnabled = true
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this, "권한 확인 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    binding.loginSubmitButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "로그인 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
