package com.example.protectsong

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.protectsong.databinding.ActivitySmsReportBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.*

class SmsReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySmsReportBinding
    private val uploadedFileUrls = mutableListOf<String>()
    private var imageUri: Uri? = null
    private var videoUri: Uri? = null

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val REQUEST_PERMISSIONS = 1010

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmsReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 권한 확인 및 요청
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS)
        }

        // 🔙 뒤로가기
        binding.backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        // 🔽 Spinner 설정
        binding.spinnerType.adapter = ArrayAdapter.createFromResource(
            this, R.array.report_types, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerBuilding.adapter = ArrayAdapter.createFromResource(
            this, R.array.building_names, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // ✅ 파일 첨부 버튼
        binding.imageAttachButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.videoAttachButton.setOnClickListener {
            pickVideoLauncher.launch("video/*")
        }

        // ✅ 촬영 버튼
        binding.cameraButton.setOnClickListener {
            val file = File.createTempFile("IMG_", ".jpg", getExternalFilesDir("Pictures"))
            imageUri = FileProvider.getUriForFile(this, "$packageName.provider", file)
            imageUri?.let { takePictureLauncher.launch(it) }
        }

        binding.camcorderButton.setOnClickListener {
            val file = File.createTempFile("VID_", ".mp4", getExternalFilesDir("Movies"))
            videoUri = FileProvider.getUriForFile(this, "$packageName.provider", file)
            videoUri?.let { takeVideoLauncher.launch(it) }
        }

        // ✅ 신고하기 버튼
        binding.btnSubmit.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val reportData = hashMapOf(
                "uid" to uid,
                "type" to binding.spinnerType.selectedItem.toString(),
                "building" to binding.spinnerBuilding.selectedItem.toString(),
                "content" to binding.editContent.text.toString(),
                "timestamp" to Date(),
                "files" to uploadedFileUrls
            )

            FirebaseFirestore.getInstance().collection("smsReports")
                .add(reportData)
                .addOnSuccessListener {
                    Toast.makeText(this, "신고가 접수되었습니다!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "신고 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // ✅ 갤러리에서 이미지 선택
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadFileToFirebase(it, "image") }
    }

    // ✅ 갤러리에서 비디오 선택
    private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadFileToFirebase(it, "video") }
    }

    // ✅ 카메라로 사진 촬영
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) imageUri?.let { uploadFileToFirebase(it, "image") }
    }

    // ✅ 캠코더로 비디오 촬영
    private val takeVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) videoUri?.let { uploadFileToFirebase(it, "video") }
    }

    // ✅ Firebase Storage에 파일 업로드
    private fun uploadFileToFirebase(uri: Uri, type: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val extension = when (type) {
            "image" -> ".jpg"
            "video" -> ".mp4"
            else -> ""
        }

        val fileName = "${System.currentTimeMillis()}_$type$extension"
        val tempFile = File.createTempFile("upload_", extension, cacheDir)

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = tempFile.outputStream()
            inputStream?.use { input -> outputStream.use { input.copyTo(it) } }

            val fileUri = Uri.fromFile(tempFile)
            val storageRef = FirebaseStorage.getInstance().getReference("reports/$uid/$fileName")

            storageRef.putFile(fileUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl
                        .addOnSuccessListener { downloadUrl ->
                            uploadedFileUrls.add(downloadUrl.toString())
                            Toast.makeText(this, "$type 업로드 완료", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "$type URL 요청 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                            Log.e("FirebaseUpload", "URL 요청 실패", it)
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "$type 업로드 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    Log.e("FirebaseUpload", "업로드 실패", it)
                }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "$type 파일 처리 오류", Toast.LENGTH_SHORT).show()
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS && !hasPermissions()) {
            Toast.makeText(this, "권한이 거부되어 촬영 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
