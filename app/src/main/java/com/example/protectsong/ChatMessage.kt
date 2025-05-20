package com.example.protectsong.model

import com.google.firebase.Timestamp

data class ChatMessage(
    val text: String = "",
    val time: Timestamp = Timestamp.now(),
    val senderId: String = "",
    val receiverId: String = "" // ✅ 추가
)