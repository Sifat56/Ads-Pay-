package com.adspay.app.data.models

import androidx.annotation.Keep

@Keep
enum class UserRole {
    USER,
    ADMIN
}

@Keep
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val points: Double = 0.0,
    val totalEarned: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    val completedQuizzesCount: Int = 0,
    val currentCycleQuizzes: Int = 0,
    val referralCode: String = "",
    val referredBy: String? = null,
    val role: UserRole = UserRole.USER,
    val isBlocked: Boolean = false,
    val isTaskDisabled: Boolean = false,
    val isWithdrawDisabled: Boolean = false,
    val isReferralDisabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis()
)
