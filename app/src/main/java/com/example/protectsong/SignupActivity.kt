package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Firebase 초기화
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 🔧 캐시 문제 방지 (테스트 중 비활성화 권장)
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()

        // 🔙 뒤로가기 버튼
        binding.backButton.setOnClickListener {
            finish()
        }

        // ✅ 회원가입 버튼 클릭
        binding.signupSubmitButton.setOnClickListener {
            val name = binding.nameEdit.text.toString().trim()
            val email = binding.emailEdit.text.toString().trim()
            val password = binding.passwordEdit.text.toString().trim()
            val studentId = binding.studentIdEdit.text.toString().trim()

            // 🔸 빈칸 확인
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || studentId.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔸 비밀번호 길이 제한
            if (password.length < 6 || password.length > 12) {
                Toast.makeText(this, "비밀번호는 6자 이상 12자 이하로 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔸 학번 숫자 7자리 검사
            val studentIdRegex = Regex("^[0-9]{7}$")
            if (!studentId.matches(studentIdRegex)) {
                Toast.makeText(this, "학번은 숫자 7자리로 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("SignupDebug", "입력된 학번: '$studentId'")

            // 🔸 학번 중복 검사
            firestore.collection("users")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener { documents ->
                    Log.d("SignupDebug", "중복 문서 수: ${documents.size()}")
                    for (doc in documents) {
                        Log.d("SignupDebug", "중복 문서 ID: ${doc.id}, studentId: ${doc.get("studentId")}, type=${doc.get("studentId")?.javaClass?.name}")
                    }

                    if (!documents.isEmpty) {
                        Toast.makeText(this, "이미 사용 중인 학번입니다.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // 🔸 중복 없음 → Firebase Auth로 계정 생성
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { result ->
                            val uid = result.user?.uid ?: return@addOnSuccessListener

                            // ✅ Firestore에 사용자 정보 저장 + 역할 포함
                            val userMap = hashMapOf(
                                "name" to name,
                                "email" to email,
                                "studentId" to studentId.toString(),  // 🔹 문자열로 강제
                                "role" to "user"
                            )

                            firestore.collection("users").document(uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "회원가입 완료", Toast.LENGTH_SHORT).show()

                                    // 👉 UserInfoActivity로 이동
                                    val intent = Intent(this, UserInfoActivity::class.java)
                                    intent.putExtra("uid", uid)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Firestore 저장 실패", Toast.LENGTH_SHORT).show()
                                    Log.e("Signup", "Firestore 저장 오류", it)
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "회원가입 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                            Log.e("Signup", "Auth 등록 오류", it)
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "학번 중복 검사 실패", Toast.LENGTH_SHORT).show()
                    Log.e("Signup", "학번 중복 검사 오류", it)
                }
        }
    }
}
