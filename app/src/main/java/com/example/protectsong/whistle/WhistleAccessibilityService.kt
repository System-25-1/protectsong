package com.example.protectsong.whistle

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class WhistleAccessibilityService : AccessibilityService() {

    private var volumeUpPressedTime: Long = 0L

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    if (volumeUpPressedTime == 0L) {
                        volumeUpPressedTime = System.currentTimeMillis()
                    }
                }
                KeyEvent.ACTION_UP -> {
                    val duration = System.currentTimeMillis() - volumeUpPressedTime
                    volumeUpPressedTime = 0L

                    if (duration >= 5000) {
                        Log.d("WhistleService", "🔊 볼륨 업 5초 이상 - 휘슬 실행")
                        val intent = Intent(this, WhistleService::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startForegroundService(intent)
                    } else {
                        Log.d("WhistleService", "🔇 볼륨 업 짧음 - 무시")
                    }
                }
            }
            return true
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 반드시 구현해야 함 (비워도 됨)
    }

    override fun onServiceConnected() {
        Log.d("WhistleService", "접근성 서비스 연결됨")
    }

    override fun onInterrupt() {}
}
