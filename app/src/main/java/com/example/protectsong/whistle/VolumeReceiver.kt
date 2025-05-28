package com.example.protectsong.whistle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent
import android.widget.Toast

class VolumeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (Intent.ACTION_MEDIA_BUTTON == action) {
            val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            if (keyEvent?.keyCode == KeyEvent.KEYCODE_VOLUME_UP && keyEvent.action == KeyEvent.ACTION_DOWN) {
                // 서비스 실행 (호루라기 소리 재생)
                Toast.makeText(context, "볼륨 업 감지됨 - 호루라기 실행", Toast.LENGTH_SHORT).show()
                val serviceIntent = Intent(context, WhistleService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
