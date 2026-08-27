package com.adspay.app.data.models

import androidx.annotation.Keep

@Keep
data class AdminAuditLog(
    val id: String = "",
    val adminId: String = "",
    val adminEmail: String = "",
    val action: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val previousValue: String = "",
    val newValue: String = "",
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
