package com.example.protectsong

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바를 액션바로 설정
        setSupportActionBar(binding.toolbar)

        // DrawerToggle 설정
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // NavigationView 메뉴 클릭
        binding.navView.setNavigationItemSelectedListener { item ->
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
    }
}
