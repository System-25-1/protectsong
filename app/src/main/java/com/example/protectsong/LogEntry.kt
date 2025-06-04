package com.example.protectsong.model

data class LogEntry(
    val userId: String = "",
    val action: String = "",
    val detail: String = "",
    val timestamp: Long = 0L
)
