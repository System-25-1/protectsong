package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityUserInfoBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class UserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserInfoBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    // 관리자 UID 복사한 값 (Firebase 콘솔 > Authentication에서 복사)
    private val adminUid = "MecPxatzCTMeHztzELY4ps4KVeh2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase 초기화
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        val isAdmin = currentUser?.uid == adminUid

        // ✅ 관리자 계정이면 사용자 정보 입력 생략하고 바로 이동
        if (isAdmin) {
            val adminInfo = mapOf(

                "name" to "관리자",
                "phone" to "",
                "birth" to "",
                "studentId" to "admin",
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
                    startActivity(Intent(this, MainActivity::class.java)) // ✅ MainActivity로 이동
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "관리자 등록 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }

            return
        }

        // 🧍‍♀️ 일반 사용자 흐름
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.relationship_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.relationshipSpinner.adapter = adapter

        binding.saveButton.isEnabled = false


        binding.backText.setOnClickListener {
            finish()
        }


        binding.verifyPhoneButton.setOnClickListener {
            val phoneNumber = binding.phoneEdit.text.toString()

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

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        this@UserInfoActivity.verificationId = verificationId
                        Toast.makeText(this@UserInfoActivity, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                })
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        binding.checkCodeButton.setOnClickListener {
            val code = binding.verificationCodeEdit.text.toString()

            if (verificationId == null || code.isEmpty()) {
                Toast.makeText(this, "인증번호를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        }

        binding.saveButton.setOnClickListener {
            val uid = intent.getStringExtra("uid") ?: return@setOnClickListener

            val phone = binding.phoneEdit.text.toString()
            val name = binding.nameEdit.text.toString()
            val birth = binding.birthEdit.text.toString()
            val studentId = binding.studentIdEdit.text.toString()

            val guardianName = binding.guardianNameEdit.text.toString()
            val guardianPhone = binding.guardianPhoneEdit.text.toString()
            val guardianRelation = binding.relationshipSpinner.selectedItem.toString()

            if (phone.isEmpty() || name.isEmpty() || birth.isEmpty() || studentId.isEmpty()) {
                Toast.makeText(this, "필수 항목을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updateMap = mapOf(
                "phone" to phone,
                "birth" to birth,
                "studentId" to studentId,
                "guardian" to mapOf(
                    "name" to guardianName,
                    "phone" to guardianPhone,
                    "relation" to guardianRelation
                )
            )

            firestore.collection("users").document(uid)
                .update(updateMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "정보 저장 완료!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UserInfo", "Firestore 저장 오류", it)
                }
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
}
