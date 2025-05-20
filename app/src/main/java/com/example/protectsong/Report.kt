<<<<<<< HEAD
package com.example.protectsong

data class Report(
    val id: String = "",
    val building: String = "",
    val content: String = "",
    val type: String = "",
    val timestamp: String = "",
    val uid: String = ""
=======
package com.example.protectsong.model

data class Report(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val number: String = "",
    val content: String = "",
    val status: String = "접수됨"
>>>>>>> feature/eunseo
)
