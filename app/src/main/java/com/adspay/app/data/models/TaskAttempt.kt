package com.adspay.app.data.models

import androidx.annotation.Keep

@Keep
data class TaskAttempt(
    val id: String = "",
    val userId: String = "",
    val quizId: String = "",
    val startTime: Long = 0L,
    val completedTime: Long = 0L,
    val selectedOptionIndex: Int = -1,
    val isCorrect: Boolean = false,
    val isVerified: Boolean = false,
    val cycleIndex: Int = 1,
    val token: String = ""
)
