package com.example.protectsong

import android.content.ComponentName
import android.os.Bundle
import android.content.Intent
import android.content.SharedPreferences
import android.content.Context
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.protectsong.accessibility.UnifiedAccessibilityService

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

        // 앱 버전 표시는 한 번만 하면 됨
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
        gpsTextView.text = if (isGpsEnabled) "켜짐" else "꺼짐"
    }

    private fun updateVoiceRecognitionStatus() {
        val voiceStatusView = findViewById<TextView>(R.id.tv_voice_recognition_status)
        val isEnabled = isAccessibilityServiceEnabled()
        voiceStatusView.text = if (isEnabled) "켜짐" else "꺼짐"
    }
    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val myService = ComponentName(this, UnifiedAccessibilityService::class.java)
        val expected = myService.flattenToString()
        return enabledServices.split(":").any { it == expected }
    }

    private fun checkVoiceRecognitionSetting(): Boolean {
        val prefs: SharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return prefs.getBoolean("voice_recognition_enabled", false)
    }
}

