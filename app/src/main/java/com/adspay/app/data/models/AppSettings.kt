package com.adspay.app.data.models

import androidx.annotation.Keep

@Keep
data class AppSettings(
    val rewardCycleQuizzesCount: Int = 5,
    val quizTimerSeconds: Int = 10,
    val rewardPointsPerCycle: Double = 1.0,
    val pointMonetaryValue: Double = 0.20,
    val currencySymbol: String = "৳",
    val referralCommissionPercent: Double = 10.0,
    val minWithdrawalPoints: Double = 50.0,
    val maxWithdrawalPoints: Double = 10000.0,
    val dailyTaskLimit: Int = 100,
    val hourlyTaskLimit: Int = 20,
    val isRegistrationEnabled: Boolean = true,
    val isLoginEnabled: Boolean = true,
    val isTaskSystemEnabled: Boolean = true,
    val isBannerAdsEnabled: Boolean = true,
    val isRewardedAdsEnabled: Boolean = true,
    val isReferralEnabled: Boolean = true,
    val isWithdrawEnabled: Boolean = true,
    val isBkashEnabled: Boolean = true,
    val isNagadEnabled: Boolean = true,
    val isUsdtEnabled: Boolean = true,
    val isLeaderboardEnabled: Boolean = true,
    val isNotificationsEnabled: Boolean = true,
    val isMaintenanceMode: Boolean = false,
    val maintenanceMessage: String = "Ads Pay is currently undergoing scheduled maintenance. Please check back shortly!",
    val appName: String = "Ads Pay",
    val startIoAppId: String = "207226080",
    val announcementText: String = "🔥 Welcome to Ads Pay! Complete 5 quizzes to watch a rewarded ad and earn points instantly! Fast withdrawals via bKash, Nagad & USDT BEP20.",
    val telegramUrl: String = "https://t.me/adspayofficial",
    val youtubeUrl: String = "https://youtube.com/@adspayofficial",
    val supportContact: String = "support@adspay.app",
    val aboutText: String = "Ads Pay is a trusted, transparent reward application where users can complete short interactive quizzes, watch sponsored ads powered by Start.io, and redeem real rewards with low minimum cash-out thresholds.",
    val howToWorkText: String = "1. Tap 'Start Task' to begin a quiz session.\n2. Answer each quiz and wait for the 10-second countdown timer.\n3. After completing 5 consecutive valid quizzes, a Start.io Rewarded Video Ad will appear.\n4. Watch the entire video ad until the completion checkmark appears.\n5. Once verified, 1 point is automatically credited to your balance!\n6. Invite friends using your referral code to get 10% lifetime commission on their earnings.\n7. Go to 'Withdraw' to redeem your points anytime via bKash, Nagad, or BEP20 USDT."
)
