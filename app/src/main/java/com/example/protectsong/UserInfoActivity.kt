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

        binding.birthEdit.addTextChangedListener(birthTextWatcher(binding.birthEdit))
        binding.phoneEdit.addTextChangedListener(phoneTextWatcher(binding.phoneEdit))
        binding.guardianPhoneEdit.addTextChangedListener(phoneTextWatcher(binding.guardianPhoneEdit))

        binding.studentIdEdit.addTextChangedListener(object : TextWatcher {
            private var previousText = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s?.toString() ?: ""
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.length > 7) {
                    binding.studentIdEdit.removeTextChangedListener(this)
                    binding.studentIdEdit.setText(previousText)
                    binding.studentIdEdit.setSelection(previousText.length)
                    binding.studentIdEdit.addTextChangedListener(this)
                    Toast.makeText(this@UserInfoActivity, "학번은 최대 7자리까지 입력할 수 있습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        })


        binding.passwordEdit.addTextChangedListener(passwordWatcher)
        binding.passwordConfirmEdit.addTextChangedListener(passwordWatcher)


        // 전화번호 입력 포맷팅 (예: 010-1234-5678)
        binding.phoneEdit.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var deletingHyphen = false
            private var hyphenStart = 0
            private var deletingBackward = false
            private var previousText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s?.toString() ?: ""
                if (count > 0 && after == 0) {
                    val deletedChar = s?.get(start)
                    deletingHyphen = deletedChar == '-'
                    hyphenStart = start
                    deletingBackward = true
                } else {
                    deletingHyphen = false
                    deletingBackward = false
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val digits = s.toString().replace("-", "")

                // 🔹 11자 초과 시 입력 차단 및 이전 상태로 복원
                if (digits.length > 11) {
                    Toast.makeText(binding.root.context, "전화번호는 최대 11자리까지 입력할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    binding.phoneEdit.removeTextChangedListener(this)
                    binding.phoneEdit.setText(previousText)
                    binding.phoneEdit.setSelection(previousText.length)
                    binding.phoneEdit.addTextChangedListener(this)
                    isFormatting = false
                    return
                }

                val formatted = when {
                    digits.length >= 11 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7, 11)}"
                    digits.length >= 7 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
                    digits.length >= 4 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
                    else -> digits
                }

                binding.phoneEdit.removeTextChangedListener(this)
                binding.phoneEdit.setText(formatted)

                val newCursorPos = when {
                    deletingHyphen && hyphenStart > 0 -> hyphenStart - 1
                    else -> formatted.length
                }

                binding.phoneEdit.setSelection(newCursorPos.coerceAtMost(formatted.length))
                binding.phoneEdit.addTextChangedListener(this)

                isFormatting = false
            }
        })

        //보호자정보포맷팅
        binding.guardianPhoneEdit.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var deletingHyphen = false
            private var hyphenStart = 0
            private var deletingBackward = false
            private var previousText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s?.toString() ?: ""
                if (count > 0 && after == 0) {
                    val deletedChar = s?.get(start)
                    deletingHyphen = deletedChar == '-'
                    hyphenStart = start
                    deletingBackward = true
                } else {
                    deletingHyphen = false
                    deletingBackward = false
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val digits = s.toString().replace("-", "")

                // 🔹 11자 초과 시 입력 차단 및 이전 상태로 복원
                if (digits.length > 11) {
                    Toast.makeText(binding.root.context, "전화번호는 최대 11자리까지 입력할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    binding.guardianPhoneEdit.removeTextChangedListener(this)
                    binding.guardianPhoneEdit.setText(previousText)
                    binding.guardianPhoneEdit.setSelection(previousText.length)
                    binding.guardianPhoneEdit.addTextChangedListener(this)
                    isFormatting = false
                    return
                }

                val formatted = when {
                    digits.length >= 11 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7, 11)}"
                    digits.length >= 7 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
                    digits.length >= 4 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
                    else -> digits
                }

                binding.guardianPhoneEdit.removeTextChangedListener(this)
                binding.guardianPhoneEdit.setText(formatted)

                val newCursorPos = when {
                    deletingHyphen && hyphenStart > 0 -> hyphenStart - 1
                    else -> formatted.length
                }

                binding.guardianPhoneEdit.setSelection(newCursorPos.coerceAtMost(formatted.length))
                binding.guardianPhoneEdit.addTextChangedListener(this)

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
            val phone = binding.phoneEdit.text.toString()
            val name = binding.nameEdit.text.toString()
            val birth = binding.birthEdit.text.toString()
            val studentId = binding.studentIdEdit.text.toString()
            val password = binding.passwordEdit.text.toString()
            val passwordConfirm = binding.passwordConfirmEdit.text.toString()
            val guardianName = binding.guardianNameEdit.text.toString()
            val guardianPhone = binding.guardianPhoneEdit.text.toString()
            val guardianRelation = binding.relationshipSpinner.selectedItem.toString()

            if (phone.isEmpty() || name.isEmpty() || birth.isEmpty() || studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "필수 항목을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (studentId.length != 7) {
                Toast.makeText(this, "학번은 7자리로 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isPasswordValid(password)) {
                binding.passwordWarning.visibility = View.VISIBLE
                return@setOnClickListener
            } else {
                binding.passwordWarning.visibility = View.GONE
            }

            if (password != passwordConfirm) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firestore.collection("users")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        Toast.makeText(this, "이미 사용 중인 학번입니다.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    } else {
                        createFirebaseUserAndSaveInfo(
                            phone, name, birth, studentId, password,
                            guardianName, guardianPhone, guardianRelation
                        )
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "학번 중복 확인 중 오류가 발생했습니다: ${it.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UserInfo", "학번 중복 체크 오류", it)
                }
        }
    }

    private val passwordWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            val password = binding.passwordEdit.text.toString()
            val confirm = binding.passwordConfirmEdit.text.toString()
            if (password.isNotEmpty() && confirm.isNotEmpty()) {
                if (password != confirm) {
                    binding.passwordMismatchText.visibility = View.VISIBLE
                    binding.saveButton.isEnabled = false
                } else {
                    binding.passwordMismatchText.visibility = View.GONE
                    binding.saveButton.isEnabled = true
                }
            } else {
                binding.passwordMismatchText.visibility = View.GONE
                binding.saveButton.isEnabled = false
            }
        }
    }

    // Firebase Authentication을 통해 사용자 생성 후 Firestore에 정보 저장

    private fun createFirebaseUserAndSaveInfo(
        phone: String, name: String, birth: String, studentId: String, password: String,
        guardianName: String, guardianPhone: String, guardianRelation: String
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

    private fun birthTextWatcher(view: View): TextWatcher = object : TextWatcher {
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
            (view as? View)?.let {
                binding.birthEdit.setText(formatted)
                binding.birthEdit.setSelection(formatted.length)
            }
            isEditing = false
        }
    }

    private fun phoneTextWatcher(view: View): TextWatcher = object : TextWatcher {
        private var isFormatting = false
        private var deletingHyphen = false
        private var hyphenStart = 0
        private var previousText = ""

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            previousText = s?.toString() ?: ""
            if (count > 0 && after == 0) {
                val deletedChar = s?.get(start)
                deletingHyphen = deletedChar == '-'
                hyphenStart = start
            } else {
                deletingHyphen = false
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (isFormatting) return
            isFormatting = true

            val digits = s.toString().replace("-", "")

            if (digits.length > 11) {
                Toast.makeText(view.context, "전화번호는 최대 11자리까지 입력할 수 있습니다.", Toast.LENGTH_SHORT).show()
                (view as? View)?.let {
                    val editText = it as android.widget.EditText
                    editText.removeTextChangedListener(this)
                    editText.setText(previousText)
                    editText.setSelection(previousText.length)
                    editText.addTextChangedListener(this)
                }
                isFormatting = false
                return
            }

            val formatted = when {
                digits.length >= 11 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7, 11)}"
                digits.length >= 7 -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
                digits.length >= 4 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
                else -> digits
            }

            (view as? View)?.let {
                val editText = it as android.widget.EditText
                editText.removeTextChangedListener(this)
                editText.setText(formatted)
                val newCursorPos = if (deletingHyphen && hyphenStart > 0) hyphenStart - 1 else formatted.length
                editText.setSelection(newCursorPos.coerceAtMost(formatted.length))
                editText.addTextChangedListener(this)
            }
            isFormatting = false
        }
    }
}