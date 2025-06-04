package com.example.protectsong.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.protectsong.whistle.WhistleService

class UnifiedAccessibilityService : AccessibilityService() {

    // 🔹 Morse 관련
    private val volumePattern = mutableListOf<Char>()
    private var lastKeyTime = 0L
    private val PATTERN_TIMEOUT = 3000L
    private val SOS_PATTERN = listOf('u', 'u', 'u', 'd', 'd', 'd', 'u', 'u', 'u')

    // 🔹 Whistle 관련
    private var volumeUpPressedTime = 0L
    private var volumeDownPressedTime = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = serviceInfo
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        serviceInfo = info
        Toast.makeText(this, "지키송 접근성 서비스 작동 중", Toast.LENGTH_SHORT).show()
        Log.d("UnifiedService", "접근성 서비스 연결됨 / flags=${info.flags}")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN && event.action != KeyEvent.ACTION_UP) return false

        handleMorsePattern(event)
        handleWhistleControl(event)

        return true // 이벤트 소비
    }

    // 🔸 모스부호 패턴 인식
    private fun handleMorsePattern(event: KeyEvent) {
        if (event.action != KeyEvent.ACTION_DOWN) return

        val now = System.currentTimeMillis()
        if (now - lastKeyTime > PATTERN_TIMEOUT) {
            volumePattern.clear()
        }
        lastKeyTime = now

        when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> volumePattern.add('u')
            KeyEvent.KEYCODE_VOLUME_DOWN -> volumePattern.add('d')
            else -> return
        }

        if (volumePattern.size >= 9 && volumePattern.takeLast(9) == SOS_PATTERN) {
            volumePattern.clear()
            callEmergencyNumber()
        }
    }

    private fun callEmergencyNumber() {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:112")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "전화 권한이 없어 신고할 수 없습니다.", Toast.LENGTH_LONG).show()
        }
    }

    // 🔸 휘슬 제어
    private fun handleWhistleControl(event: KeyEvent) {
        when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP   -> handleVolumeUp(event)
            KeyEvent.KEYCODE_VOLUME_DOWN -> handleVolumeDown(event)
        }
    }

    private fun handleVolumeUp(event: KeyEvent) {
        when (event.action) {
            KeyEvent.ACTION_DOWN -> if (volumeUpPressedTime == 0L) volumeUpPressedTime = System.currentTimeMillis()
            KeyEvent.ACTION_UP -> {
                val dur = System.currentTimeMillis() - volumeUpPressedTime
                volumeUpPressedTime = 0L
                if (dur >= 5000L) {
                    Log.d("UnifiedService", "🔊 볼륨 업 5초 이상 - 휘슬 실행")
                    launchWhistleService()
                }
            }
        }
    }

    private fun handleVolumeDown(event: KeyEvent) {
        when (event.action) {
            KeyEvent.ACTION_DOWN -> if (volumeDownPressedTime == 0L) volumeDownPressedTime = System.currentTimeMillis()
            KeyEvent.ACTION_UP -> {
                val dur = System.currentTimeMillis() - volumeDownPressedTime
                volumeDownPressedTime = 0L
                if (dur >= 3000L) {
                    Log.d("UnifiedService", "🔇 볼륨 다운 3초 이상 - 휘슬 중지")
                    stopWhistleService()
                }
            }
        }
    }

    private fun launchWhistleService() {
        Intent(this, WhistleService::class.java).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(this)
            else startService(this)
        }
    }

    private fun stopWhistleService() {
        stopService(Intent(this, WhistleService::class.java))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
