package com.example.protectsong

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CallActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val phone = intent.getStringExtra("phone")
        if (phone.isNullOrBlank()) {
            Toast.makeText(this, "전화번호가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone"))

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent)
        } else {
            Toast.makeText(this, "전화 권한이 없어 신고할 수 없습니다.", Toast.LENGTH_LONG).show()
        }

        finish() // 통화 시도 후 종료
    }
}
