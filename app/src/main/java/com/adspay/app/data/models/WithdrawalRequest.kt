package com.adspay.app.data.models

import androidx.annotation.Keep

@Keep
enum class WithdrawMethod {
    BKASH,
    NAGAD,
    USDT_BEP20
}

@Keep
enum class WithdrawalStatus {
    PENDING,
    PROCESSING,
    APPROVED,
    PAID,
    REJECTED,
    CANCELLED
}

@Keep
data class WithdrawalRequest(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val points: Double = 0.0,
    val amountCurrency: Double = 0.0,
    val currencySymbol: String = "৳",
    val method: WithdrawMethod = WithdrawMethod.BKASH,
    val accountInfo: String = "",
    val accountHolderName: String = "",
    val status: WithdrawalStatus = WithdrawalStatus.PENDING,
    val requestDate: Long = System.currentTimeMillis(),
    val processedDate: Long? = null,
    val adminNote: String = ""
)
