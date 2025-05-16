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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase 초기화
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Spinner 어댑터 연결
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.relationship_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.relationshipSpinner.adapter = adapter

        // 기본으로 저장 버튼 비활성화 (인증 후 활성화)
        binding.saveButton.isEnabled = false

        // 뒤로가기
        binding.backButton.setOnClickListener {
            finish()
        }

        // 인증 요청 버튼
        binding.verifyPhoneButton.setOnClickListener {
            val phoneNumber = binding.phoneEdit.text.toString()

            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "전화번호를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+82${phoneNumber.drop(1)}") // 010 → +8210
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

        // 인증번호 확인 버튼
        binding.checkCodeButton.setOnClickListener {
            val code = binding.verificationCodeEdit.text.toString()


            if (verificationId == null || code.isEmpty()) {
                Toast.makeText(this, "인증번호를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        }

        //저장 버튼 클릭 시 Firestore 저장
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
