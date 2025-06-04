package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityUserInfoBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.concurrent.TimeUnit

class UserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserInfoBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    private val adminUid = "Os1oJCzG45OKwyglRdc0JXxbghw2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        val isAdmin = currentUser?.uid == adminUid

        // 관리자 자동 등록 로직
        if (isAdmin) {
            val adminInfo = mapOf(
                "name" to "관리자",
                "phone" to "",
                "birth" to "",
                "studentId" to "admin",
                "role" to "admin",
                "guardian" to mapOf(
                    "name" to "",
                    "phone" to "",
                    "relation" to ""
                )
            )

            firestore.collection("users").document(adminUid)
                .set(adminInfo)
                .addOnSuccessListener {
                    Toast.makeText(this, "관리자 자동 등록 완료", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "관리자 등록 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            return
        }

        // 보호자 관계 스피너 설정
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.relationship_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.relationshipSpinner.adapter = adapter

        // 기본적으로 저장 버튼 비활성화 (전화번호 인증 후 활성화)
        binding.saveButton.isEnabled = false

        binding.backText.setOnClickListener {
            finish()
        }

        // 생년월일 입력 포맷팅
        binding.birthEdit.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true
                val digits = s.toString().replace(".", "").take(8)
                val formatted = when {
                    digits.length >= 7 -> "${digits.substring(0, 4)}.${digits.substring(4, 6)}.${digits.substring(6)}"
                    digits.length >= 5 -> "${digits.substring(0, 4)}.${digits.substring(4)}"
                    digits.length >= 1 -> digits
                    else -> ""
                }
                binding.birthEdit.setText(formatted)
                binding.birthEdit.setSelection(formatted.length)
                isEditing = false
            }
        })

        // 전화번호 입력 포맷팅 (예: 010-1234-5678)
        binding.phoneEdit.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val digits = s.toString().replace("-", "")
                val formatted = when {
                    digits.length >= 11 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7, 11)}"
                    digits.length >= 7 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
                    digits.length >= 4 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
                    else -> digits
                }

                binding.phoneEdit.setText(formatted)
                binding.phoneEdit.setSelection(formatted.length)
                isFormatting = false
            }
        })
        //보호자정보포맷팅
        binding.guardianPhoneEdit.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val digits = s.toString().replace("-", "")
                val formatted = when {
                    digits.length >= 11 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7, 11)}"
                    digits.length >= 7 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
                    digits.length >= 4 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
                    else -> digits
                }

                binding.guardianPhoneEdit.setText(formatted)
                binding.guardianPhoneEdit.setSelection(formatted.length)
                isFormatting = false
            }
        })



        // 전화번호 인증 요청
        binding.verifyPhoneButton.setOnClickListener {
            val phoneNumber = binding.phoneEdit.text.toString().replace("-", "")
            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "전화번호를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+82${phoneNumber.drop(1)}")
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        signInWithPhoneAuthCredential(credential)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Toast.makeText(this@UserInfoActivity, "인증 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        this@UserInfoActivity.verificationId = verificationId
                        Toast.makeText(this@UserInfoActivity, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                })
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        // 인증코드 확인
        binding.checkCodeButton.setOnClickListener {
            val code = binding.verificationCodeEdit.text.toString()
            if (verificationId == null || code.isEmpty()) {
                Toast.makeText(this, "인증번호를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        }

        // 저장 버튼 클릭 시 (회원가입 및 정보 저장)
        binding.saveButton.setOnClickListener {
            val phone = binding.phoneEdit.text.toString()
            val name = binding.nameEdit.text.toString()
            val birth = binding.birthEdit.text.toString()
            val studentId = binding.studentIdEdit.text.toString()
            val password = binding.passwordEdit.text.toString()
            val passwordConfirm = binding.passwordConfirmEdit.text.toString()
            val guardianName = binding.guardianNameEdit.text.toString()
            val guardianPhone = binding.guardianPhoneEdit.text.toString()
            val guardianRelation = binding.relationshipSpinner.selectedItem.toString()

            // 필수 항목 입력 확인
            if (phone.isEmpty() || name.isEmpty() || birth.isEmpty() || studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "필수 항목을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 학번 7자리 제한 검사
            if (studentId.length != 7) {
                Toast.makeText(this, "학번은 7자리로 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 비밀번호 유효성 검사
            if (!isPasswordValid(password)) {
                binding.passwordWarning.visibility = View.VISIBLE
                return@setOnClickListener
            } else {
                binding.passwordWarning.visibility = View.GONE
            }

            // 비밀번호 확인 일치 여부
            if (password != passwordConfirm) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firestore에서 동일 학번 존재 여부 확인
            firestore.collection("users")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // 이미 같은 학번이 존재할 경우
                        Toast.makeText(this, "이미 사용 중인 학번입니다.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    } else {
                        // 중복이 없으면 회원가입 진행
                        createFirebaseUserAndSaveInfo(
                            phone,
                            name,
                            birth,
                            studentId,
                            password,
                            guardianName,
                            guardianPhone,
                            guardianRelation
                        )
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "학번 중복 확인 중 오류가 발생했습니다: ${it.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UserInfo", "학번 중복 체크 오류", it)
                }
        }
    }

    // Firebase Authentication을 통해 사용자 생성 후 Firestore에 정보 저장
    private fun createFirebaseUserAndSaveInfo(
        phone: String,
        name: String,
        birth: String,
        studentId: String,
        password: String,
        guardianName: String,
        guardianPhone: String,
        guardianRelation: String
    ) {
        val fakeEmail = "$studentId@protectsong.app"
        auth.createUserWithEmailAndPassword(fakeEmail, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val userInfo = mapOf(
                    "phone" to phone,
                    "name" to name,
                    "birth" to birth,
                    "studentId" to studentId,
                    "role" to "user",
                    "guardian" to mapOf(
                        "name" to guardianName,
                        "phone" to guardianPhone,
                        "relation" to guardianRelation
                    )
                )

                firestore.collection("users").document(uid)
                    .set(userInfo)
                    .addOnSuccessListener {
                        Toast.makeText(this, "정보 저장 완료!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                        Log.e("UserInfo", "Firestore 저장 오류", it)
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "회원가입 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                Log.e("UserInfo", "회원가입 실패", it)
            }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                Toast.makeText(this, "전화번호 인증 완료", Toast.LENGTH_SHORT).show()
                binding.saveButton.isEnabled = true
            }
            .addOnFailureListener {
                Toast.makeText(this, "인증 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isPasswordValid(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)[A-Za-z\\d]{8,16}$")
        return regex.matches(password)
    }
}
