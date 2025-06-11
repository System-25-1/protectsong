package com.example.protectsong

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityAdminSmsMainBinding
import com.example.protectsong.model.SmsReport
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminSmsMainBinding
    private lateinit var adapter: AdminPagedReportAdapter
    private lateinit var toggle: ActionBarDrawerToggle

    private val db = FirebaseFirestore.getInstance()
    private var currentPage = 1
    private val itemsPerPage = 10
    private var allReports = listOf<SmsReport>()
    private var filteredReports = listOf<SmsReport>()
    private var reportListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSmsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        logAdminAction("관리자 홈 접속", "AdminMainActivity에 접속함")

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, android.R.color.white)

        val headerView = binding.navView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvStudentId = headerView.findViewById<TextView>(R.id.tvStudentId)
        val logoutButton = headerView.findViewById<TextView>(R.id.logout_button)
        val tvSettings = headerView.findViewById<TextView>(R.id.tvSettings)
        val tvMyProfile = headerView.findViewById<TextView>(R.id.tvMyProfile)
        val tvMyReport = headerView.findViewById<TextView>(R.id.tvMyReport)
        val tvLogCheck = headerView.findViewById<TextView>(R.id.tvLogCheck)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("AdminMainDebug", "[1] currentUser.uid = $uid")

        if (uid != null) {
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    Log.d("AdminMainDebug", "[2] doc.exists() = ${doc.exists()}")
                    Log.d("AdminMainDebug", "[3] doc.data = ${doc.data}")
                    val nameVal = doc.getString("name")
                    val idVal = doc.getString("studentId")
                    val isAdmin = doc.getBoolean("isAdmin") ?: false

                    tvUserName.text = nameVal ?: "이름 없음"
                    tvStudentId.text = idVal ?: "학번 없음"

                    if (isAdmin) {
                        tvSettings.visibility = View.GONE
                        tvMyProfile.visibility = View.GONE
                        tvMyReport.visibility = View.GONE
                        tvLogCheck.visibility = View.VISIBLE
                    } else {
                        tvSettings.visibility = View.VISIBLE
                        tvMyProfile.visibility = View.VISIBLE
                        tvMyReport.visibility = View.VISIBLE
                        tvLogCheck.visibility = View.GONE
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AdminMainDebug", "[5] Firestore read failed", e)
                    Toast.makeText(this, "헤더 정보 불러오기 실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        logoutButton.setOnClickListener {
            logAdminAction("로그아웃", "관리자 로그아웃")
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

        tvLogCheck.setOnClickListener {
            startActivity(Intent(this, LogListActivity::class.java))
        }

        adapter = AdminPagedReportAdapter { report ->
            logAdminAction("신고 상세 보기", "reportId: ${report.id}")
            val intent = Intent(this, AdminReportDetailActivity::class.java)
            intent.putExtra("report", report)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = applyFilters()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatListActivity::class.java))
                    true
                }
                R.id.nav_home -> true
                R.id.nav_post -> {
                    startActivity(Intent(this, PostListActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun startListeningReports() {
        reportListener = db.collection("smsReports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("FirestoreListener", "SnapshotListener error", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    allReports = snapshots.map { doc ->
                        SmsReport(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            type = doc.getString("type") ?: "",
                            building = doc.getString("building") ?: "",
                            content = doc.getString("content") ?: "",
                            status = doc.getString("status") ?: "접수됨",
                            files = doc.get("files") as? List<String> ?: emptyList(),
                            timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                        )
                    }
                    applyFilters()
                } else {
                    Log.e("FirestoreListener", "snapshots is null")
                }
            }
    }

    private fun applyFilters() {
        val keyword = binding.etSearch.text.toString().trim()
        val selectedStatus = binding.spinnerStatus.selectedItem.toString()

        logAdminAction("검색/필터", "검색어: '$keyword', 상태: '$selectedStatus'")

        lifecycleScope.launch(Dispatchers.Default) {
            val filtered = allReports.filter { report ->
                val matchesKeyword = keyword.isEmpty() || report.content.contains(keyword, ignoreCase = true)
                val matchesStatus = selectedStatus == "전체" || report.status == selectedStatus
                matchesKeyword && matchesStatus
            }

            withContext(Dispatchers.Main) {
                filteredReports = filtered
                currentPage = 1
                updatePagedData()
                updatePaginationButtons()
            }
        }
    }

    private fun updatePagedData() {
        val start = (currentPage - 1) * itemsPerPage
        val end = (start + itemsPerPage).coerceAtMost(filteredReports.size)
        val paged = filteredReports.subList(start, end)
        adapter.submitList(paged)
        updatePageLabel()
    }

    private fun updatePaginationButtons() {
        val container = binding.pageNumberContainer
        container.removeAllViews()
        val totalPages = getTotalPages()

        if (currentPage > 1) {
            val prev = TextView(this).apply {
                text = "< 이전"
                textSize = 16f
                setPadding(20, 0, 20, 0)
                minWidth = 100
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#002366"))
                setOnClickListener {
                    currentPage--
                    updatePagedData()
                    updatePaginationButtons()
                }
            }
            container.addView(prev)
        }

        for (i in 1..totalPages) {
            val tv = TextView(this).apply {
                text = "$i"
                textSize = 16f
                setPadding(12, 0, 12, 0)
                minWidth = 48
                gravity = Gravity.CENTER
                setTextColor(if (i == currentPage) Color.BLUE else Color.DKGRAY)
                setOnClickListener {
                    currentPage = i
                    updatePagedData()
                    updatePaginationButtons()
                }
            }
            container.addView(tv)
        }

        if (currentPage < totalPages) {
            val next = TextView(this).apply {
                text = "다음 >"
                textSize = 16f
                setPadding(20, 0, 0, 0)
                setTextColor(Color.parseColor("#002366"))
                minWidth = 100
                gravity = Gravity.CENTER
                setOnClickListener {
                    currentPage++
                    updatePagedData()
                    updatePaginationButtons()
                }
            }
            container.addView(next)
        }
    }

    private fun updatePageLabel() {
        val total = getTotalPages()
        binding.tvPage.text = "페이지 $currentPage / $total"
    }

    private fun getTotalPages(): Int {
        return if (filteredReports.isEmpty()) 1 else ((filteredReports.size - 1) / itemsPerPage + 1)
    }

    override fun onStart() {
        super.onStart()
        startListeningReports()
    }

    override fun onStop() {
        super.onStop()
        reportListener?.remove()
    }

    private fun logAdminAction(action: String, detail: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val log = hashMapOf(
            "userId" to uid,
            "action" to action,
            "detail" to detail,
            "timestamp" to FieldValue.serverTimestamp()
        )
        FirebaseFirestore.getInstance().collection("logs").add(log)
            .addOnFailureListener { e ->
                Log.e("LogSystem", "로그 저장 실패: ${e.message}")
            }
    }
}
