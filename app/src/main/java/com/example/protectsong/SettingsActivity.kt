package com.example.protectsong

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Button
import android.content.Intent
import android.net.Uri
import android.provider.Settings


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<TextView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_go_to_settings).setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        // GPS 상태 확인 예시 (단순 표시용)
        val gpsStatus = "켜짐" // 실제 상태 가져오려면 LocationManager 활용
        findViewById<TextView>(R.id.tv_gps_status).text = gpsStatus

        // 앱 버전 표시
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        findViewById<TextView>(R.id.tv_app_version).text = "V $versionName"
    }
}

