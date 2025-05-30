package com.example.protectsong

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.protectsong.databinding.ActivitySmsReportBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.*

class SmsReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySmsReportBinding
    private val uploadedFileUrls = mutableListOf<String>()
    private val attachedThumbnails = mutableMapOf<View, String>()
    private var imageUri: Uri? = null
    private var videoUri: Uri? = null
    private var totalUploadSizeMB = 0L

    private lateinit var uploadStatusLayout: LinearLayout
    private lateinit var uploadStatusText: TextView
    private lateinit var uploadInfoText: TextView

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val REQUEST_PERMISSIONS = 1010

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmsReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uploadStatusLayout = findViewById(R.id.uploadStatusLayout)
        uploadStatusText = findViewById(R.id.uploadStatusText)
        uploadInfoText = findViewById(R.id.uploadInfoText)
        updateUploadInfo()

        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS)
        }

        binding.toolbarInclude.backText.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            finish()
        }

        binding.spinnerType.adapter = ArrayAdapter.createFromResource(
            this, R.array.report_types, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerBuilding.adapter = ArrayAdapter.createFromResource(
            this, R.array.building_names, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.imageAttachButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.videoAttachButton.setOnClickListener {
            pickVideoLauncher.launch("video/*")
        }

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

        binding.btnSubmit.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val reportData = hashMapOf(
                "uid" to uid,
                "type" to binding.spinnerType.selectedItem.toString(),
                "building" to binding.spinnerBuilding.selectedItem.toString(),
                "content" to binding.editContent.text.toString(),
                "timestamp" to Date(),
                "files" to uploadedFileUrls,
                "status" to "접수됨"
            )

            FirebaseFirestore.getInstance().collection("smsReports")
                .add(reportData)
                .addOnSuccessListener {
                    Toast.makeText(this, "신고가 접수되었습니다!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
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

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadFileToFirebase(it, "image") }
    }

    private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadFileToFirebase(it, "video") }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) imageUri?.let { uploadFileToFirebase(it, "image") }
    }

    private val takeVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) videoUri?.let { uploadFileToFirebase(it, "video") }
    }

    private fun getFileSizeInMB(uri: Uri): Long {
        val cursor = contentResolver.query(uri, null, null, null, null)
        val sizeIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.SIZE) ?: -1
        var sizeInBytes: Long = 0
        if (cursor != null && sizeIndex != -1) {
            cursor.moveToFirst()
            sizeInBytes = cursor.getLong(sizeIndex)
            cursor.close()
        }
        return sizeInBytes / (1024 * 1024)
    }

    private fun updateUploadInfo() {
        uploadInfoText.text = "( 파일 ${uploadedFileUrls.size}개, ${"%.1f".format(totalUploadSizeMB.toFloat())}MB / 50MB )"
    }

    private fun uploadFileToFirebase(uri: Uri, type: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val fileSizeMB = getFileSizeInMB(uri)

        if (totalUploadSizeMB + fileSizeMB > 50) {
            Toast.makeText(this, "파일 용량은 총 50MB까지만 가능합니다", Toast.LENGTH_SHORT).show()
            return
        }
        totalUploadSizeMB += fileSizeMB

        uploadStatusText.text = if (type == "image") "이미지 업로드 중..." else "동영상 업로드 중..."
        uploadStatusLayout.visibility = View.VISIBLE

        val extension = if (type == "image") ".jpg" else ".mp4"
        val fileName = "${System.currentTimeMillis()}_${type}${extension}"
        val tempFile = File.createTempFile("upload_", extension, cacheDir)

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = tempFile.outputStream()
            inputStream?.use { input -> outputStream.use { input.copyTo(it) } }

            val fileUri = Uri.fromFile(tempFile)
            val storageRef = FirebaseStorage.getInstance().getReference("reports/$uid/$fileName")

            storageRef.putFile(fileUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        uploadedFileUrls.add(downloadUrl.toString())
                        addThumbnail(uri, downloadUrl.toString(), type)
                        uploadStatusLayout.visibility = View.GONE
                        updateUploadInfo()
                        Toast.makeText(this, "$type 업로드 완료!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    uploadStatusLayout.visibility = View.GONE
                    Toast.makeText(this, "$type 업로드 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            e.printStackTrace()
            uploadStatusLayout.visibility = View.GONE
            Toast.makeText(this, "$type 파일 처리 오류", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addThumbnail(uri: Uri, fileUrl: String, type: String) {
        val container = findViewById<LinearLayout>(R.id.attachedThumbnailsContainer)

        val layout = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                setMargins(8, 8, 8, 8)
            }
        }

        val thumbnail = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        if (type == "image") {
            thumbnail.setImageURI(uri)
        } else {
            Glide.with(this)
                .load(uri)
                .frame(1000000L)
                .into(thumbnail)
        }

        layout.addView(thumbnail)

        if (type == "video") {
            val playIcon = ImageView(this).apply {
                setImageResource(R.drawable.ic_play_circle_overlay)
                layoutParams = FrameLayout.LayoutParams(60, 60, Gravity.CENTER)
            }
            layout.addView(playIcon)
        }

        val deleteBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            background = null
            layoutParams = FrameLayout.LayoutParams(60, 60).apply {
                gravity = Gravity.END
            }
            setOnClickListener {
                removeThumbnail(layout, fileUrl)
            }
        }

        layout.addView(deleteBtn)
        container.addView(layout)
        attachedThumbnails[layout] = fileUrl
    }

    private fun removeThumbnail(view: View, fileUrl: String) {
        uploadedFileUrls.remove(fileUrl)
        val container = findViewById<LinearLayout>(R.id.attachedThumbnailsContainer)
        container.removeView(view)

        FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl)
            .metadata
            .addOnSuccessListener { metadata ->
                val sizeMB = metadata.sizeBytes / (1024 * 1024)
                totalUploadSizeMB -= sizeMB
                updateUploadInfo()
            }

        FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "파일 삭제 완료", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "파일 삭제 실패", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS && !hasPermissions()) {
            Toast.makeText(this, "권한이 거부되어 촬영 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
