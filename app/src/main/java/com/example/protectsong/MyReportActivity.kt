package com.example.protectsong

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.adapter.ReportAdapter
import com.example.protectsong.databinding.ActivityMyReportBinding
import com.example.protectsong.model.Report
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MyReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyReportBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ReportAdapter

    private val pageSize = 5
    private var selectedPage = 1
    private var allDocuments: List<DocumentSnapshot> = emptyList()

    private var startDate: Date? = null
    private var endDate: Date? = null
    private var keyword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        adapter = ReportAdapter()

        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReports.adapter = adapter

        // 뒤로가기
        binding.backText.setOnClickListener { finish() }

        // 날짜 선택기
        binding.etDateStart.setOnClickListener {
            binding.etDateStart.clearFocus()
            showDatePicker { date ->
                startDate = date
                binding.etDateStart.setText(formatDate(date))
            }
        }

        binding.etDateEnd.setOnClickListener {
            binding.etDateEnd.clearFocus()
            showDatePicker { date ->
                endDate = date
                binding.etDateEnd.setText(formatDate(date))
            }
        }

        // 검색 버튼
        binding.btnSearch.setOnClickListener {
            keyword = binding.etContent.text.toString().trim()
            selectedPage = 1
            fetchReports()
        }

        // 초기 로딩
        fetchReports()
    }

    private fun fetchReports() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("smsReports")
            .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                allDocuments = snapshot.documents.filter { doc ->
                    val report = doc.toObject(Report::class.java)
                    val ts = report?.timestamp?.toDate()

                    val inDateRange = (startDate == null || (ts != null && ts >= startDate!!)) &&
                            (endDate == null || (ts != null && ts <= endOf(endDate!!)))

                    val matchKeyword = keyword.isBlank() || report?.content?.contains(keyword, true) == true

                    inDateRange && matchKeyword
                }
                loadPagedReports(selectedPage)
            }
            .addOnFailureListener {
                Toast.makeText(this, "불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPagedReports(page: Int) {
        selectedPage = page
        val fromIndex = (page - 1) * pageSize
        val toIndex = minOf(page * pageSize, allDocuments.size)

        if (fromIndex >= allDocuments.size) {
            adapter.submitList(emptyList())
            drawPagination()
            return
        }

        val pageDocs = allDocuments.subList(fromIndex, toIndex)
        val reports = pageDocs.mapNotNull { doc ->
            doc.toObject(Report::class.java)?.apply { id = doc.id }
        }

        adapter.submitList(reports)
        drawPagination()
    }

    private fun drawPagination() {
        val totalPages = (allDocuments.size + pageSize - 1) / pageSize
        val layout = binding.layoutPagination
        layout.removeAllViews()

        for (i in 1..totalPages) {
            val tv = TextView(this).apply {
                text = "$i"
                textSize = 16f
                setPadding(12, 0, 12, 0)
                setTextColor(if (i == selectedPage) Color.BLUE else Color.DKGRAY)
                setOnClickListener { loadPagedReports(i) }
            }
            layout.addView(tv)
        }

        if (selectedPage < totalPages) {
            val next = TextView(this).apply {
                text = "다음 >"
                textSize = 16f
                setPadding(20, 0, 0, 0)
                setTextColor(Color.parseColor("#002366"))
                setOnClickListener { loadPagedReports(selectedPage + 1) }
            }
            layout.addView(next)
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            cal.set(y, m, d, 0, 0, 0)
            onDateSelected(cal.time)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
        return sdf.format(date)
    }

    private fun endOf(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.time
    }
}
