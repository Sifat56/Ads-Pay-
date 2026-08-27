package com.adspay.app.data.models

import androidx.annotation.Keep

@Keep
data class Quiz(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int = 0,
    val timerSeconds: Int = 10,
    val category: String = "General",
    val isActive: Boolean = true,
    val order: Int = 0
)
