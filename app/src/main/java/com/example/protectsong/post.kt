package com.example.protectsong

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

data class Post(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "",
    val timestamp: Timestamp? = null
)  : java.io.Serializable {
    val date: String
        get() = timestamp?.toDate()?.let {
            SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(it)
        } ?: ""

    val isNotice: Boolean
        get() = category == "공지"
}
