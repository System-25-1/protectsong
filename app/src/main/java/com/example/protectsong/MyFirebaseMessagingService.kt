package com.example.protectsong

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.content.Context
import com.example.protectsong.R

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body

        if (title != null && body != null) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Android 8 이상: 채널 등록
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "default",
                    "기본 알림 채널",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                manager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.ic_notification) // 🔔 알림 아이콘 필요
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)

            manager.notify(0, builder.build())
        }
    }
}
