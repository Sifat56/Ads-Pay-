package com.adspay.app.data.models

import androidx.annotation.Keep

@Keep
enum class TransactionType {
    REWARD_CYCLE,
    REFERRAL_BONUS,
    MANUAL_ADJUSTMENT,
    WITHDRAWAL_DEDUCT,
    WITHDRAWAL_REFUND,
    SIGNUP_BONUS
}

@Keep
data class RewardTransaction(
    val id: String = "",
    val userId: String = "",
    val points: Double = 0.0,
    val type: TransactionType = TransactionType.REWARD_CYCLE,
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val referenceId: String = ""
)
