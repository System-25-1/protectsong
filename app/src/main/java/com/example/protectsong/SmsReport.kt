package com.example.protectsong.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SmsReport(
    val id: String = "",
    val uid: String = "",
    val type: String = "",
    val building: String = "",
    val content: String = "",
    val status: String = "접수됨",
    val files: List<String> = emptyList(),
    val timestamp: Long = 0L // ✅ timestamp를 Long(ms)로 저장
) : Parcelable
