package com.example.protectsong

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private var isWhistleOn = false

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🟦 툴바 및 DrawerLayout 설정
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        val titleText = findViewById<TextView>(R.id.toolbarTitle)
        titleText.text = "지키송"

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 🔸 NavigationView 메뉴 클릭 처리
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_mypage -> {
                    Toast.makeText(this, "마이페이지 클릭됨", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "설정 클릭됨", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_logout -> {
                    Toast.makeText(this, "로그아웃 클릭됨", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // 🟦 기존 버튼들 설정
        val btnEmergency = findViewById<ImageButton>(R.id.btnEmergency)
        val btnWhistle = findViewById<ImageButton>(R.id.btnWhistle)
        val tvWhistle = findViewById<TextView>(R.id.tvWhistle)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val ivCall = findViewById<ImageView>(R.id.ivCall)

        btnEmergency.setOnClickListener {
            Toast.makeText(this, "긴급 신고 버튼이 눌렸습니다!", Toast.LENGTH_SHORT).show()
        }

        btnWhistle.setOnClickListener {
            isWhistleOn = !isWhistleOn
            btnWhistle.isSelected = isWhistleOn
            tvWhistle.text = if (isWhistleOn) "on" else "off"
        }

        ivCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:010-8975-0220")
            startActivity(intent)
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    Toast.makeText(this, "Chat 탭 선택됨", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_home -> {
                    Toast.makeText(this, "Home 탭 선택됨", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_post -> {
                    Toast.makeText(this, "Post 탭 선택됨", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}
