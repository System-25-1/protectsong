package com.example.protectsong

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityAdminSmsMainBinding
import com.example.protectsong.model.SmsReport
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

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

        // ✅ 툴바 설정
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // ✅ 드로어 토글
        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, android.R.color.white)

        // ✅ 드로어 헤더
        val headerView = binding.navView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvStudentId = headerView.findViewById<TextView>(R.id.tvStudentId)
        val logoutButton = headerView.findViewById<TextView>(R.id.logout_button)
        val tvSettings = headerView.findViewById<TextView>(R.id.tvSettings)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    tvUserName.text = doc.getString("name") ?: "이름 없음"
                    tvStudentId.text = doc.getString("studentId") ?: "학번 없음"
                }
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

        tvSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // ✅ 리사이클러뷰 설정
        adapter = AdminPagedReportAdapter { report ->
            val intent = Intent(this, AdminReportDetailActivity::class.java)
            intent.putExtra("report", report)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // ✅ 필터링 기능 연결
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = applyFilters()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long
            ) = applyFilters()

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        setupPaginationControls()

        // ✅ 하단 네비게이션
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
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) {
                    Toast.makeText(this, "데이터를 불러오는 중 오류 발생", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                allReports = snapshots.map { doc ->
                    SmsReport(
                        id = doc.id,
                        uid = doc.getString("uid") ?: "",
                        type = doc.getString("type") ?: "",
                        building = doc.getString("building") ?: "",
                        content = doc.getString("content") ?: "",
                        status = doc.getString("status") ?: "접수됨",
                        files = doc.get("files") as? List<String> ?: emptyList(),
                        timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                    )
                }
                applyFilters()
            }
    }

    private fun applyFilters() {
        val keyword = binding.etSearch.text.toString().trim()
        val selectedStatus = binding.spinnerStatus.selectedItem.toString()

        filteredReports = allReports.filter { report ->
            val matchesKeyword = keyword.isEmpty() || report.content.contains(keyword, ignoreCase = true)
            val matchesStatus = selectedStatus == "전체" || report.status == selectedStatus
            matchesKeyword && matchesStatus
        }

        currentPage = 1
        updatePagedData()
        updatePaginationButtons()
    }

    private fun updatePagedData() {
        val start = (currentPage - 1) * itemsPerPage
        val end = (start + itemsPerPage).coerceAtMost(filteredReports.size)
        val paged = filteredReports.subList(start, end)
        adapter.submitList(paged)
        updatePageLabel()
    }

    private fun setupPaginationControls() {
        binding.btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                updatePagedData()
            }
        }

        binding.btnNext.setOnClickListener {
            if ((currentPage * itemsPerPage) < filteredReports.size) {
                currentPage++
                updatePagedData()
            }
        }
    }

    private fun updatePaginationButtons() {
        binding.pageNumberContainer.removeAllViews()
        val totalPages = getTotalPages()

        // ⬅ 이전 버튼
        binding.pageNumberContainer.addView(binding.btnPrev)

        for (i in 1..totalPages) {
            val btn = Button(this).apply {
                text = i.toString()
                setOnClickListener {
                    currentPage = i
                    updatePagedData()
                }
            }
            binding.pageNumberContainer.addView(btn)
        }

        // ➡ 다음 버튼
        binding.pageNumberContainer.addView(binding.btnNext)
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
}
