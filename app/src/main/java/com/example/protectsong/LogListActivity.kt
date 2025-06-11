package com.example.protectsong

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityLogListBinding
import com.example.protectsong.model.LogEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class LogListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogListBinding
    private val db = FirebaseFirestore.getInstance()
    private val logList = mutableListOf<LogEntry>()
    private var filteredLogList = listOf<LogEntry>()
    private lateinit var adapter: LogListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = LogListAdapter(logList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        fetchLogs()

        binding.btnExportCsv.setOnClickListener {
            exportLogsToCsv()
        }

        binding.etSearchStudentId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = filterLogs()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.includeToolbar.backText.setOnClickListener { finish() }
        binding.includeToolbar.titleText.text = "로그 목록"
    }

    private fun fetchLogs() {
        db.collection("logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                logList.clear()
                for (doc in result) {
                    val uid = doc.getString("userId") ?: ""
                    val action = doc.getString("action") ?: ""
                    val detail = doc.getString("detail") ?: ""
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L

                    db.collection("users").document(uid)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val studentId = userDoc.getString("studentId") ?: ""
                            logList.add(
                                LogEntry(
                                    userId = uid,
                                    studentId = studentId,
                                    action = action,
                                    detail = detail,
                                    timestamp = timestamp
                                )
                            )
                            filteredLogList = logList.toList()
                            adapter.submitList(filteredLogList)
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "로그 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterLogs() {
        val keyword = binding.etSearchStudentId.text.toString().trim()
        filteredLogList = logList.filter {
            it.studentId.contains(keyword, ignoreCase = true)
        }
        adapter.submitList(filteredLogList)
    }

    private fun exportLogsToCsv() {
        if (filteredLogList.isEmpty()) {
            Toast.makeText(this, "내보낼 로그가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val studentId = filteredLogList.firstOrNull()?.studentId ?: "unknown"
        val fileName = "protectsong_${studentId}.csv"
        val csvFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

        try {
            // ✅ UTF-8 BOM 추가를 위한 OutputStreamWriter 사용
            val outputStream = csvFile.outputStream()
            val writer = outputStream.bufferedWriter(Charsets.UTF_8)

            // ✅ BOM (Byte Order Mark) 추가 - 엑셀에서 한글 안 깨지도록
            writer.write('\uFEFF'.toString())

            writer.write("studentId,action,detail,timestamp\n")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            for (log in filteredLogList) {
                writer.write("\"${log.studentId}\",\"${log.action}\",\"${log.detail}\",\"${sdf.format(Date(log.timestamp))}\"\n")
            }

            writer.flush()
            writer.close()

            val uri: Uri = FileProvider.getUriForFile(this, "$packageName.provider", csvFile)
            Toast.makeText(this, "CSV 파일이 저장되었습니다:\n${csvFile.absolutePath}", Toast.LENGTH_LONG).show()

            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/csv")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                startActivity(Intent.createChooser(openIntent, "CSV 파일 열기"))
            } catch (e: Exception) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "CSV 파일 공유하기"))
            }

        } catch (e: Exception) {
            Toast.makeText(this, "CSV 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



}
