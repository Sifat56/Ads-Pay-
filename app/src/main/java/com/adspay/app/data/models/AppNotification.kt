package com.adspay.app.data.models

import androidx.annotation.Keep

@Keep
data class AppNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "ANNOUNCEMENT",
    val targetUserId: String = "ALL",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
