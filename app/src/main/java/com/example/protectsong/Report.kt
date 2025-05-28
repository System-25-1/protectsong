package com.example.protectsong.model

import com.google.firebase.Timestamp

data class Report(
    var id: String? = null, // ✅ 문서 ID를 위한 필드 추가
    val uid: String? = null,
    val content: String? = null,
    val building: String? = null,
    val type: String? = null,
    val timestamp: Timestamp? = null,
    val status: String? = null,
    val files: Any? = null
)
