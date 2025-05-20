package com.example.protectsong.model

import com.example.protectsong.model.ChatMessage

sealed class ChatDisplayItem {
    data class DateHeader(val dateText: String) : ChatDisplayItem()
    data class MessageItem(val message: ChatMessage) : ChatDisplayItem()
}
