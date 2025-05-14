package com.example.protectsong.model

data class ChatMessage(
    val text: String,
    val time: String,
    val isSent: Boolean // true: 내가 보낸 메시지, false: 상대방 메시지
)
