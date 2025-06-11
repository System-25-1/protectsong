package com.example.protectsong

import android.os.Bundle
import android.content.Intent
import android.content.Context
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton = findViewById<TextView>(R.id.backText)
        backButton.setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_go_to_settings).setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        // 앱 버전 표시
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        val versionText = getString(R.string.version_text, versionName)
        findViewById<TextView>(R.id.tv_app_version).text = versionText
    }

    override fun onResume() {
        super.onResume()
        updateGpsStatus()
        updateVoiceRecognitionStatus()
    }

    private fun updateGpsStatus() {
        val gpsTextView = findViewById<TextView>(R.id.tv_gps_status)
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        gpsTextView.text = if (isGpsEnabled && hasPermission) "켜짐" else "꺼짐"
    }


    private fun updateVoiceRecognitionStatus() {
        val voiceStatusView = findViewById<TextView>(R.id.tv_voice_recognition_status)
        val micPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val isMicAllowed = micPermission == PackageManager.PERMISSION_GRANTED
        voiceStatusView.text = if (isMicAllowed) "켜짐" else "꺼짐"
    }
}
