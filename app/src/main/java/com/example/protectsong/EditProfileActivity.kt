package com.example.protectsong

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.protectsong.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var selectedImageUri: Uri? = null
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        // 🔙 뒤로가기
        binding.backText.setOnClickListener { finish() }

        // 🔽 보호자 관계 스피너 설정
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.relationship_options,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerRelation.adapter = adapter

        // 갤러리 이미지 선택
        setupProfileImagePicker()

        // Firestore에서 사용자 정보 불러오기
        loadUserInfo()

        // 수정 버튼
        binding.btnUpdate.setOnClickListener {
            saveGuardianInfo()
        }
    }

    // 🖼️ 갤러리 이미지 선택 런처
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.profileImage.setImageURI(it)
            uploadProfileImageToFirebase(it)
        }
    }

    private fun setupProfileImagePicker() {
        binding.selectPhotoText.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    // 🔍 Firestore에서 사용자 정보 불러오기
    private fun loadUserInfo() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val docRef = firestore.collection("users").document(uid)

        docRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                binding.apply {
                    tvUserName.text = document.getString("name") ?: ""
                    tvBirthdate.text = document.getString("birth") ?: ""
                    tvPhone.text = document.getString("phone") ?: ""
                    tvStudentId.text = document.getString("studentId") ?: ""

                    val guardian = document.get("guardian") as? Map<*, *>
                    editGuardianName.setText(guardian?.get("name")?.toString() ?: "")
                    editGuardianPhone.setText(guardian?.get("phone")?.toString() ?: "")
                    val relation = guardian?.get("relation")?.toString() ?: ""
                    val pos = (spinnerRelation.adapter as ArrayAdapter<String>).getPosition(relation)
                    spinnerRelation.setSelection(pos)
                }

                // 프로필 이미지 불러오기
                val imageRef = FirebaseStorage.getInstance().getReference("profile_images/$uid.jpg")
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(this@EditProfileActivity)
                        .load(uri)
                        .into(binding.profileImage)
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "사용자 정보 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 💾 보호자 정보 Firestore + SharedPreferences에 저장
    private fun saveGuardianInfo() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val guardianName = binding.editGuardianName.text.toString()
        val guardianPhone = binding.editGuardianPhone.text.toString()
        val guardianRelation = binding.spinnerRelation.selectedItem.toString()

        val updateMap = mapOf(
            "guardian" to mapOf(
                "name" to guardianName,
                "phone" to guardianPhone,
                "relation" to guardianRelation
            )
        )

        firestore.collection("users").document(uid)
            .update(updateMap)
            .addOnSuccessListener {
                // SharedPreferences 저장
                val prefs = getSharedPreferences("guardian_info", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("guardian_name", guardianName)
                    putString("guardian_phone", guardianPhone)
                    putString("guardian_relation", guardianRelation)
                    apply()
                }

                Toast.makeText(this, "수정되었습니다", Toast.LENGTH_SHORT).show()
                // ✅ MainActivity로 이동
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish() // 현재 EditProfileActivity 종료

            }
            .addOnFailureListener {
                Toast.makeText(this, "정보 수정 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ☁️ 프로필 사진 Firebase Storage 업로드
    private fun uploadProfileImageToFirebase(uri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().getReference("profile_images/$uid.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                Toast.makeText(this, "사진 업로드 완료", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "사진 업로드 실패", Toast.LENGTH_SHORT).show()
            }
    }
}
