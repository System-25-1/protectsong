package com.example.protectsong.model

import com.google.firebase.Timestamp

data class ChatMessage(
    val text: String = "",
    val mediaUrl: String? = null,     // 촬영한 이미지/영상의 URL
    val mediaType: String? = null,    // "image" 또는 "video"
    val time: Timestamp = Timestamp.now(),
    val senderId: String = "",
    val receiverId: String = ""
)
