package com.example.protectsong.whistle

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class WhistleAccessibilityService : AccessibilityService() {

    private var volumeUpPressedTime = 0L
    private var volumeDownPressedTime = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        // 시스템 전역 키 이벤트 필터링 요청
        val info = serviceInfo
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        serviceInfo = info
        Log.d("WhistleService", "접근성 서비스 연결됨 / flags=${info.flags}")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP   -> handleVolumeUp(event)
            KeyEvent.KEYCODE_VOLUME_DOWN -> handleVolumeDown(event)
            else                         -> return super.onKeyEvent(event)
        }
        // true 로 이벤트 소비
        return true
    }

    private fun handleVolumeUp(event: KeyEvent) {
        when (event.action) {
            KeyEvent.ACTION_DOWN -> if (volumeUpPressedTime == 0L) volumeUpPressedTime = System.currentTimeMillis()
            KeyEvent.ACTION_UP   -> {
                val dur = System.currentTimeMillis() - volumeUpPressedTime
                volumeUpPressedTime = 0L
                if (dur >= 5000L) {
                    Log.d("WhistleService", "🔊 볼륨 업 5초 이상 - 휘슬 실행")
                    launchWhistleService()
                }
            }
        }
    }

    private fun handleVolumeDown(event: KeyEvent) {
        when (event.action) {
            KeyEvent.ACTION_DOWN -> if (volumeDownPressedTime == 0L) volumeDownPressedTime = System.currentTimeMillis()
            KeyEvent.ACTION_UP   -> {
                val dur = System.currentTimeMillis() - volumeDownPressedTime
                volumeDownPressedTime = 0L
                if (dur >= 3000L) {
                    Log.d("WhistleService", "🔇 볼륨 다운 3초 이상 - 휘슬 중지")
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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) { /* 사용 안 함 */ }
    override fun onInterrupt() {}
}