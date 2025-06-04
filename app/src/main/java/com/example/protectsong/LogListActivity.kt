package com.example.protectsong

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = filterLogs(s.toString())
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.includeToolbar.backText.setOnClickListener {
            finish()
        }

        binding.includeToolbar.titleText.text = "로그 목록"
    }

    private fun fetchLogs() {
        db.collection("logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                logList.clear()
                for (doc in result) {
                    val entry = LogEntry(
                        userId = doc.getString("userId") ?: "",
                        action = doc.getString("action") ?: "",
                        detail = doc.getString("detail") ?: "",
                        timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                    )
                    logList.add(entry)
                }
                adapter.submitList(logList.toList())
            }
            .addOnFailureListener {
                Toast.makeText(this, "로그 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterLogs(keyword: String) {
        val filtered = logList.filter {
            it.userId.contains(keyword, ignoreCase = true) ||
                    it.action.contains(keyword, ignoreCase = true)
        }
        adapter.submitList(filtered)
    }

    private fun exportLogsToCsv() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1234)
            return
        }

        val csvFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "protectsong_logs.csv")
        try {
            val writer = FileWriter(csvFile)
            writer.append("userId,action,detail,timestamp\n")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            for (log in logList) {
                writer.append("\"${log.userId}\",\"${log.action}\",\"${log.detail}\",\"${sdf.format(Date(log.timestamp))}\"\n")
            }
            writer.flush()
            writer.close()

            val uri = FileProvider.getUriForFile(this, "$packageName.provider", csvFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "CSV 파일 공유하기"))

        } catch (e: Exception) {
            Toast.makeText(this, "CSV 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}