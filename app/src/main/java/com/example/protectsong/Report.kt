package com.example.protectsong.model

import java.util.Date


data class Report(
    val id: String = "",
    val building: String = "",
    val content: String = "",
    val type: String = "",
    val timestamp: String = "",
    val uid: String = "",          // 또는 userId 로 변경 (Firestore 필드 기준)
    val date: Date? = null,         // 🔍 날짜 검색 시 사용된다면 추가
    val number: String = "",       // 🔍 신고번호 검색 시 사용된다면 추가
    val status: String = ""        // 🔔 처리상태 표시 시 필요
)
