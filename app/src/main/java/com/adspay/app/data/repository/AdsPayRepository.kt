package com.adspay.app.data.repository

import android.content.Context
import com.adspay.app.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import kotlin.random.Random

object AdsPayRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _quizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    private val _userList = MutableStateFlow<List<User>>(emptyList())
    val userList: StateFlow<List<User>> = _userList.asStateFlow()

    private val _withdrawals = MutableStateFlow<List<WithdrawalRequest>>(emptyList())
    val withdrawals: StateFlow<List<WithdrawalRequest>> = _withdrawals.asStateFlow()

    private val _transactions = MutableStateFlow<List<RewardTransaction>>(emptyList())
    val transactions: StateFlow<List<RewardTransaction>> = _transactions.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AdminAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AdminAuditLog>> = _auditLogs.asStateFlow()

    // Task attempt tracking for anti-fraud
    private val activeAttempts = mutableMapOf<String, TaskAttempt>()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        val initialQuizzes = listOf(
            Quiz(
                id = "q1",
                question = "What is the primary cryptocurrency created by Satoshi Nakamoto?",
                options = listOf("Bitcoin (BTC)", "Ethereum (ETH)", "Solana (SOL)", "Dogecoin (DOGE)"),
                correctOptionIndex = 0,
                timerSeconds = 10,
                category = "Crypto",
                isActive = true,
                order = 1
            ),
            Quiz(
                id = "q2",
                question = "What does 'CPU' stand for in computer science?",
                options = listOf("Central Process Unit", "Central Processing Unit", "Computer Personal Unit", "Core Power Unit"),
                correctOptionIndex = 1,
                timerSeconds = 10,
                category = "Tech",
                isActive = true,
                order = 2
            ),
            Quiz(
                id = "q3",
                question = "What is 45 + 55 × 2?",
                options = listOf("200", "155", "145", "190"),
                correctOptionIndex = 1,
                timerSeconds = 10,
                category = "Math",
                isActive = true,
                order = 3
            ),
            Quiz(
                id = "q4",
                question = "Which mobile operating system is developed by Google?",
                options = listOf("iOS", "Android", "Windows Mobile", "Symbian"),
                correctOptionIndex = 1,
                timerSeconds = 10,
                category = "Mobile",
                isActive = true,
                order = 4
            ),
            Quiz(
                id = "q5",
                question = "How many quizzes are required to unlock a Rewarded Video Ad in Ads Pay?",
                options = listOf("3 Quizzes", "5 Quizzes", "10 Quizzes", "1 Quiz"),
                correctOptionIndex = 1,
                timerSeconds = 10,
                category = "Ads Pay",
                isActive = true,
                order = 5
            ),
            Quiz(
                id = "q6",
                question = "What is the chemical symbol for Gold?",
                options = listOf("Ag", "Au", "Fe", "Cu"),
                correctOptionIndex = 1,
                timerSeconds = 10,
                category = "Science",
                isActive = true,
                order = 6
            ),
            Quiz(
                id = "q7",
                question = "Which of the following is a fast mobile financial service in Bangladesh?",
                options = listOf("bKash", "PayPal", "Venmo", "CashApp"),
                correctOptionIndex = 0,
                timerSeconds = 10,
                category = "General",
                isActive = true,
                order = 7
            ),
            Quiz(
                id = "q8",
                question = "Which token standard does BEP20 belong to?",
                options = listOf("BNB Smart Chain (BSC)", "Ethereum Mainnet", "Bitcoin Lightning", "Polygon POS"),
                correctOptionIndex = 0,
                timerSeconds = 10,
                category = "Crypto",
                isActive = true,
                order = 8
            )
        )
        _quizzes.value = initialQuizzes

        val adminUser = User(
            id = "AP-ADMIN01",
            name = "Admin Officer",
            email = "admin@adspay.app",
            phone = "+8801700000000",
            points = 500.0,
            totalEarned = 500.0,
            referralCode = "ADMIN99",
            role = UserRole.ADMIN
        )

        val demoUser = User(
            id = "AP-" + Random.nextInt(10000, 99999),
            name = "Sifat Islam",
            email = "sifat@example.com",
            phone = "+8801812345678",
            points = 24.0,
            totalEarned = 75.0,
            totalWithdrawn = 50.0,
            completedQuizzesCount = 18,
            currentCycleQuizzes = 2,
            referralCode = "PAY" + Random.nextInt(1000, 9999),
            role = UserRole.USER
        )

        val user2 = User(
            id = "AP-77312",
            name = "Tanvir Ahmed",
            email = "tanvir@example.com",
            phone = "+8801912345678",
            points = 142.0,
            totalEarned = 210.0,
            totalWithdrawn = 68.0,
            completedQuizzesCount = 95,
            currentCycleQuizzes = 4,
            referralCode = "PAY7731",
            role = UserRole.USER
        )

        val user3 = User(
            id = "AP-88421",
            name = "Rahim Mia",
            email = "rahim@example.com",
            phone = "+8801612345678",
            points = 88.0,
            totalEarned = 150.0,
            totalWithdrawn = 62.0,
            completedQuizzesCount = 60,
            currentCycleQuizzes = 1,
            referralCode = "PAY8842",
            role = UserRole.USER
        )

        _userList.value = listOf(adminUser, demoUser, user2, user3)
        _currentUser.value = demoUser

        // Seed transactions
        val initialTx = listOf(
            RewardTransaction(
                id = "tx1",
                userId = demoUser.id,
                points = 1.0,
                type = TransactionType.REWARD_CYCLE,
                title = "Rewarded Ad Completed",
                description = "Completed 5 quizzes cycle and verified Start.io rewarded ad",
                timestamp = System.currentTimeMillis() - 3600000 * 2
            ),
            RewardTransaction(
                id = "tx2",
                userId = demoUser.id,
                points = 5.0,
                type = TransactionType.SIGNUP_BONUS,
                title = "Welcome Bonus",
                description = "New account onboarding reward",
                timestamp = System.currentTimeMillis() - 86400000 * 2
            ),
            RewardTransaction(
                id = "tx3",
                userId = demoUser.id,
                points = 2.0,
                type = TransactionType.REFERRAL_BONUS,
                title = "Referral Commission",
                description = "10% commission from referral task completions",
                timestamp = System.currentTimeMillis() - 86400000
            )
        )
        _transactions.value = initialTx

        // Seed sample withdrawals
        val sampleWithdrawals = listOf(
            WithdrawalRequest(
                id = "w1",
                userId = demoUser.id,
                userName = demoUser.name,
                userEmail = demoUser.email,
                points = 50.0,
                amountCurrency = 10.0,
                currencySymbol = "৳",
                method = WithdrawMethod.BKASH,
                accountInfo = "01812345678",
                accountHolderName = "Sifat Islam",
                status = WithdrawalStatus.PAID,
                requestDate = System.currentTimeMillis() - 86400000 * 3,
                processedDate = System.currentTimeMillis() - 86400000 * 2,
                adminNote = "Paid successfully via bKash TrxID: 9X82JKA"
            ),
            WithdrawalRequest(
                id = "w2",
                userId = user2.id,
                userName = user2.name,
                userEmail = user2.email,
                points = 100.0,
                amountCurrency = 20.0,
                currencySymbol = "৳",
                method = WithdrawMethod.NAGAD,
                accountInfo = "01912345678",
                accountHolderName = "Tanvir Ahmed",
                status = WithdrawalStatus.PENDING,
                requestDate = System.currentTimeMillis() - 3600000,
                adminNote = ""
            )
        )
        _withdrawals.value = sampleWithdrawals

        // Seed notifications
        val sampleNotifications = listOf(
            AppNotification(
                id = "n1",
                title = "Welcome to Ads Pay 🎉",
                message = "Earn real cash by completing 5 simple quizzes and watching Start.io rewarded video ads.",
                type = "ANNOUNCEMENT",
                timestamp = System.currentTimeMillis() - 86400000
            ),
            AppNotification(
                id = "n2",
                title = "Withdrawal Paid Successfully 💰",
                message = "Your bKash withdrawal for ৳10.00 (50 points) has been successfully processed!",
                type = "WITHDRAWAL",
                targetUserId = demoUser.id,
                timestamp = System.currentTimeMillis() - 86400000 * 2
            )
        )
        _notifications.value = sampleNotifications
    }

    // --- Authentication ---
    fun login(emailOrPhone: String, password: String):Result<User> {
        if (!_appSettings.value.isLoginEnabled) {
            return Result.failure(Exception("User login is currently disabled by Admin."))
        }
        val cleanInput = emailOrPhone.trim().lowercase()
        val user = _userList.value.find { 
            it.email.lowercase() == cleanInput || it.phone == emailOrPhone.trim() 
        }

        if (user != null) {
            if (user.isBlocked) {
                return Result.failure(Exception("Your account has been suspended by administration."))
            }
            _currentUser.value = user
            return Result.success(user)
        }

        // Auto-create for demo/testing convenience if not existing
        val newUser = User(
            id = "AP-" + Random.nextInt(10000, 99999),
            name = if (cleanInput.contains("@")) cleanInput.substringBefore("@").replaceFirstChar { it.uppercase() } else "User",
            email = if (cleanInput.contains("@")) cleanInput else "$cleanInput@adspay.app",
            phone = if (!cleanInput.contains("@")) cleanInput else "+88017" + Random.nextInt(10000000, 99999999),
            points = 0.0, // Initial 0 points balance
            totalEarned = 0.0,
            referralCode = "PAY" + Random.nextInt(1000, 9999),
            role = UserRole.USER
        )
        _userList.update { it + newUser }
        _currentUser.value = newUser
        return Result.success(newUser)
    }

    fun register(name: String, email: String, phone: String, password: String, referralCode: String?): Result<User> {
        if (!_appSettings.value.isRegistrationEnabled) {
            return Result.failure(Exception("Registration is currently paused by Administrator."))
        }

        if (_userList.value.any { it.email.equals(email.trim(), ignoreCase = true) }) {
            return Result.failure(Exception("An account with this email already exists."))
        }

        var referrerUser: User? = null
        if (!referralCode.isNullOrBlank()) {
            val code = referralCode.trim().uppercase()
            referrerUser = _userList.value.find { it.referralCode.uppercase() == code }
            if (referrerUser == null) {
                return Result.failure(Exception("Invalid referral code. Please check or leave empty."))
            }
        }

        val newUser = User(
            id = "AP-" + Random.nextInt(10000, 99999),
            name = name.trim(),
            email = email.trim().lowercase(),
            phone = phone.trim(),
            points = 0.0, // Newly registered users start with 0 points
            totalEarned = 0.0,
            referralCode = "PAY" + Random.nextInt(1000, 9999),
            referredBy = referrerUser?.referralCode,
            role = UserRole.USER
        )

        _userList.update { it + newUser }
        _currentUser.value = newUser

        return Result.success(newUser)
    }

    fun logout() {
        _currentUser.value = null
    }

    fun switchUserRole(role: UserRole) {
        _currentUser.update { current ->
            current?.copy(role = role)
        }
    }

    // --- Task & Quiz Management ---
    fun startTaskAttempt(quizId: String): Result<TaskAttempt> {
        val user = _currentUser.value ?: return Result.failure(Exception("Not authenticated"))
        if (user.isBlocked || user.isTaskDisabled) {
            return Result.failure(Exception("Task access is restricted for this account."))
        }
        if (!_appSettings.value.isTaskSystemEnabled) {
            return Result.failure(Exception("Task system is currently disabled by Admin."))
        }

        val attempt = TaskAttempt(
            id = UUID.randomUUID().toString(),
            userId = user.id,
            quizId = quizId,
            startTime = System.currentTimeMillis(),
            cycleIndex = user.currentCycleQuizzes + 1,
            token = UUID.randomUUID().toString()
        )
        activeAttempts[attempt.id] = attempt
        return Result.success(attempt)
    }

    fun completeQuiz(attemptId: String, selectedOption: Int): Result<QuizCompletionResult> {
        val attempt = activeAttempts[attemptId] ?: return Result.failure(Exception("Invalid or expired task attempt."))
        val user = _currentUser.value ?: return Result.failure(Exception("User not authenticated."))
        val quiz = _quizzes.value.find { it.id == attempt.quizId } ?: return Result.failure(Exception("Quiz question not found."))

        val timeElapsed = System.currentTimeMillis() - attempt.startTime
        val requiredTimeMs = (quiz.timerSeconds * 1000L) - 1000L // 1s tolerance for network latency

        if (timeElapsed < requiredTimeMs) {
            return Result.failure(Exception("Anti-fraud validation failed: Quiz completed too quickly ($timeElapsed ms). You must wait for the 10s timer."))
        }

        val isCorrect = selectedOption == quiz.correctOptionIndex
        val updatedAttempt = attempt.copy(
            completedTime = System.currentTimeMillis(),
            selectedOptionIndex = selectedOption,
            isCorrect = isCorrect,
            isVerified = true
        )
        activeAttempts.remove(attemptId)

        val newCycleCount = user.currentCycleQuizzes + 1
        val requiredCount = _appSettings.value.rewardCycleQuizzesCount

        val isRewardCycleReady = newCycleCount >= requiredCount

        _currentUser.update { current ->
            current?.copy(
                completedQuizzesCount = current.completedQuizzesCount + 1,
                currentCycleQuizzes = if (isRewardCycleReady) requiredCount else newCycleCount
            )
        }
        updateUserInList(_currentUser.value)

        return Result.success(
            QuizCompletionResult(
                isCorrect = isCorrect,
                correctIndex = quiz.correctOptionIndex,
                currentCycleProgress = if (isRewardCycleReady) requiredCount else newCycleCount,
                requiredCycleQuizzes = requiredCount,
                isRewardCycleReady = isRewardCycleReady
            )
        )
    }

    fun verifyAndClaimRewardedAd(): Result<Double> {
        val user = _currentUser.value ?: return Result.failure(Exception("User not authenticated."))
        if (user.isBlocked || user.isTaskDisabled) {
            return Result.failure(Exception("Account restricted from claiming rewards."))
        }

        val requiredCount = _appSettings.value.rewardCycleQuizzesCount
        if (user.currentCycleQuizzes < requiredCount) {
            return Result.failure(Exception("Reward cycle requirement not met (${user.currentCycleQuizzes}/$requiredCount completed)."))
        }

        val rewardPoints = _appSettings.value.rewardPointsPerCycle

        // Server-side atomic balance update
        _currentUser.update { current ->
            current?.copy(
                points = current.points + rewardPoints,
                totalEarned = current.totalEarned + rewardPoints,
                currentCycleQuizzes = 0 // Reset cycle cleanly
            )
        }
        updateUserInList(_currentUser.value)

        recordTransaction(
            userId = user.id,
            points = rewardPoints,
            type = TransactionType.REWARD_CYCLE,
            title = "Rewarded Ad Completed",
            description = "Successfully watched Start.io Rewarded Video Ad after $requiredCount valid quizzes"
        )

        // Process referral commission if referred
        user.referredBy?.let { refCode ->
            val referrer = _userList.value.find { it.referralCode == refCode }
            if (referrer != null && !referrer.isReferralDisabled && _appSettings.value.isReferralEnabled) {
                val commission = rewardPoints * (_appSettings.value.referralCommissionPercent / 100.0)
                if (commission > 0) {
                    _userList.update { list ->
                        list.map { u ->
                            if (u.id == referrer.id) {
                                u.copy(
                                    points = u.points + commission,
                                    totalEarned = u.totalEarned + commission
                                )
                            } else u
                        }
                    }
                    recordTransaction(
                        userId = referrer.id,
                        points = commission,
                        type = TransactionType.REFERRAL_BONUS,
                        title = "Referral Commission",
                        description = "10% commission from referral ${user.name}'s rewarded ad completion"
                    )
                }
            }
        }

        return Result.success(rewardPoints)
    }

    // --- Withdrawals ---
    fun requestWithdrawal(
        method: WithdrawMethod,
        points: Double,
        accountInfo: String,
        accountHolderName: String
    ): Result<WithdrawalRequest> {
        val user = _currentUser.value ?: return Result.failure(Exception("User not authenticated."))
        if (!_appSettings.value.isWithdrawEnabled) {
            return Result.failure(Exception("Withdrawals are currently disabled by administration."))
        }
        if (user.isBlocked || user.isWithdrawDisabled) {
            return Result.failure(Exception("Withdrawal access is restricted for this account."))
        }

        // Validate method enablement
        when (method) {
            WithdrawMethod.BKASH -> if (!_appSettings.value.isBkashEnabled) return Result.failure(Exception("bKash withdrawals are currently unavailable."))
            WithdrawMethod.NAGAD -> if (!_appSettings.value.isNagadEnabled) return Result.failure(Exception("Nagad withdrawals are currently unavailable."))
            WithdrawMethod.USDT_BEP20 -> if (!_appSettings.value.isUsdtEnabled) return Result.failure(Exception("USDT BEP20 withdrawals are currently unavailable."))
        }

        val minPoints = _appSettings.value.minWithdrawalPoints
        if (points < minPoints) {
            return Result.failure(Exception("Minimum withdrawal is $minPoints points."))
        }
        if (user.points < points) {
            return Result.failure(Exception("Insufficient point balance. You have ${user.points} points."))
        }
        if (accountInfo.trim().length < 6) {
            return Result.failure(Exception("Please enter a valid account number / wallet address."))
        }

        val currencyAmount = points * _appSettings.value.pointMonetaryValue

        // Atomic deduction
        _currentUser.update { current ->
            current?.copy(
                points = current.points - points,
                totalWithdrawn = current.totalWithdrawn + points
            )
        }
        updateUserInList(_currentUser.value)

        val request = WithdrawalRequest(
            id = "WTH-" + UUID.randomUUID().toString().take(8).uppercase(),
            userId = user.id,
            userName = user.name,
            userEmail = user.email,
            points = points,
            amountCurrency = currencyAmount,
            currencySymbol = _appSettings.value.currencySymbol,
            method = method,
            accountInfo = accountInfo.trim(),
            accountHolderName = accountHolderName.trim(),
            status = WithdrawalStatus.PENDING,
            requestDate = System.currentTimeMillis()
        )

        _withdrawals.update { listOf(request) + it }

        recordTransaction(
            userId = user.id,
            points = -points,
            type = TransactionType.WITHDRAWAL_DEDUCT,
            title = "Withdrawal Requested (${method.name})",
            description = "Cash out request of $points points ($currencyAmount ${_appSettings.value.currencySymbol}) to $accountInfo",
            referenceId = request.id
        )

        return Result.success(request)
    }

    // --- Admin Operations ---
    fun updateWithdrawalStatus(
        withdrawalId: String,
        newStatus: WithdrawalStatus,
        adminNote: String,
        adminId: String = "ADMIN"
    ): Result<Unit> {
        val item = _withdrawals.value.find { it.id == withdrawalId } ?: return Result.failure(Exception("Withdrawal not found"))
        val prevStatus = item.status

        _withdrawals.update { list ->
            list.map { w ->
                if (w.id == withdrawalId) {
                    w.copy(
                        status = newStatus,
                        adminNote = adminNote,
                        processedDate = System.currentTimeMillis()
                    )
                } else w
            }
        }

        // If rejected or cancelled, refund points automatically
        if (newStatus == WithdrawalStatus.REJECTED || newStatus == WithdrawalStatus.CANCELLED) {
            _userList.update { list ->
                list.map { u ->
                    if (u.id == item.userId) {
                        u.copy(
                            points = u.points + item.points,
                            totalWithdrawn = (u.totalWithdrawn - item.points).coerceAtLeast(0.0)
                        )
                    } else u
                }
            }
            if (_currentUser.value?.id == item.userId) {
                _currentUser.update { it?.copy(
                    points = (it.points + item.points),
                    totalWithdrawn = (it.totalWithdrawn - item.points).coerceAtLeast(0.0)
                ) }
            }

            recordTransaction(
                userId = item.userId,
                points = item.points,
                type = TransactionType.WITHDRAWAL_REFUND,
                title = "Withdrawal Refunded",
                description = "Refund of ${item.points} pts for rejected withdrawal ($adminNote)",
                referenceId = item.id
            )
        }

        // Notify user
        val notificationMsg = when (newStatus) {
            WithdrawalStatus.PAID -> "Your withdrawal of ${item.amountCurrency} ${item.currencySymbol} has been sent! Note: $adminNote"
            WithdrawalStatus.APPROVED -> "Your withdrawal has been approved and is being processed."
            WithdrawalStatus.PROCESSING -> "Your withdrawal request is currently in queue."
            WithdrawalStatus.REJECTED -> "Your withdrawal was rejected. Points refunded. Reason: $adminNote"
            else -> "Withdrawal status updated to ${newStatus.name}"
        }

        addNotification(
            title = "Withdrawal Update (${item.method.name})",
            message = notificationMsg,
            type = "WITHDRAWAL",
            targetUserId = item.userId
        )

        logAudit(
            adminId = adminId,
            action = "UPDATE_WITHDRAWAL_STATUS",
            targetType = "WITHDRAWAL",
            targetId = withdrawalId,
            prevValue = prevStatus.name,
            newValue = newStatus.name,
            reason = adminNote
        )

        return Result.success(Unit)
    }

    fun adjustUserPoints(
        userId: String,
        amount: Double,
        reason: String,
        adminId: String = "ADMIN"
    ): Result<Unit> {
        if (reason.isBlank()) return Result.failure(Exception("Mandatory reason is required for manual balance adjustments."))

        val targetUser = _userList.value.find { it.id == userId } ?: return Result.failure(Exception("User not found."))
        val oldPoints = targetUser.points
        val newPoints = (oldPoints + amount).coerceAtLeast(0.0)

        _userList.update { list ->
            list.map { u ->
                if (u.id == userId) {
                    u.copy(points = newPoints)
                } else u
            }
        }
        if (_currentUser.value?.id == userId) {
            _currentUser.update { it?.copy(points = newPoints) }
        }

        recordTransaction(
            userId = userId,
            points = amount,
            type = TransactionType.MANUAL_ADJUSTMENT,
            title = if (amount >= 0) "Admin Credit" else "Admin Deduction",
            description = reason
        )

        logAudit(
            adminId = adminId,
            action = "ADJUST_USER_POINTS",
            targetType = "USER",
            targetId = userId,
            prevValue = "$oldPoints pts",
            newValue = "$newPoints pts",
            reason = reason
        )

        return Result.success(Unit)
    }

    fun toggleUserRestriction(
        userId: String,
        block: Boolean? = null,
        taskDisabled: Boolean? = null,
        withdrawDisabled: Boolean? = null,
        referralDisabled: Boolean? = null,
        adminId: String = "ADMIN"
    ) {
        val target = _userList.value.find { it.id == userId } ?: return

        val updated = target.copy(
            isBlocked = block ?: target.isBlocked,
            isTaskDisabled = taskDisabled ?: target.isTaskDisabled,
            isWithdrawDisabled = withdrawDisabled ?: target.isWithdrawDisabled,
            isReferralDisabled = referralDisabled ?: target.isReferralDisabled
        )

        _userList.update { list ->
            list.map { if (it.id == userId) updated else it }
        }
        if (_currentUser.value?.id == userId) {
            _currentUser.value = updated
        }

        logAudit(
            adminId = adminId,
            action = "TOGGLE_USER_PERMISSIONS",
            targetType = "USER",
            targetId = userId,
            prevValue = "Blocked:${target.isBlocked}, TaskOff:${target.isTaskDisabled}",
            newValue = "Blocked:${updated.isBlocked}, TaskOff:${updated.isTaskDisabled}",
            reason = "Admin permission modification"
        )
    }

    fun updateAppSettings(settings: AppSettings, adminId: String = "ADMIN") {
        val prev = _appSettings.value
        _appSettings.value = settings

        logAudit(
            adminId = adminId,
            action = "UPDATE_APP_SETTINGS",
            targetType = "SETTINGS",
            targetId = "GLOBAL",
            prevValue = "Cycle:${prev.rewardCycleQuizzesCount}, Value:${prev.pointMonetaryValue}",
            newValue = "Cycle:${settings.rewardCycleQuizzesCount}, Value:${settings.pointMonetaryValue}",
            reason = "Admin settings modification"
        )
    }

    fun addQuiz(quiz: Quiz, adminId: String = "ADMIN") {
        val newQuiz = quiz.copy(id = "q_" + UUID.randomUUID().toString().take(6))
        _quizzes.update { it + newQuiz }
        logAudit(adminId, "ADD_QUIZ", "QUIZ", newQuiz.id, "", newQuiz.question, "New quiz added")
    }

    fun updateQuiz(quiz: Quiz, adminId: String = "ADMIN") {
        _quizzes.update { list ->
            list.map { if (it.id == quiz.id) quiz else it }
        }
        logAudit(adminId, "UPDATE_QUIZ", "QUIZ", quiz.id, "", quiz.question, "Quiz updated")
    }

    fun deleteQuiz(quizId: String, adminId: String = "ADMIN") {
        _quizzes.update { it.filterNot { q -> q.id == quizId } }
        logAudit(adminId, "DELETE_QUIZ", "QUIZ", quizId, "", "", "Quiz deleted")
    }

    fun addNotification(title: String, message: String, type: String = "ANNOUNCEMENT", targetUserId: String = "ALL") {
        val n = AppNotification(
            id = "notif_" + UUID.randomUUID().toString().take(6),
            title = title,
            message = message,
            type = type,
            targetUserId = targetUserId,
            timestamp = System.currentTimeMillis()
        )
        _notifications.update { listOf(n) + it }
    }

    // --- Helpers ---
    private fun updateUserInList(user: User?) {
        if (user == null) return
        _userList.update { list ->
            list.map { if (it.id == user.id) user else it }
        }
    }

    private fun recordTransaction(
        userId: String,
        points: Double,
        type: TransactionType,
        title: String,
        description: String,
        referenceId: String = ""
    ) {
        val tx = RewardTransaction(
            id = "tx_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            points = points,
            type = type,
            title = title,
            description = description,
            timestamp = System.currentTimeMillis(),
            referenceId = referenceId
        )
        _transactions.update { listOf(tx) + it }
    }

    private fun logAudit(
        adminId: String,
        action: String,
        targetType: String,
        targetId: String,
        prevValue: String,
        newValue: String,
        reason: String
    ) {
        val log = AdminAuditLog(
            id = "log_" + UUID.randomUUID().toString().take(8),
            adminId = adminId,
            adminEmail = _currentUser.value?.email ?: "admin@adspay.app",
            action = action,
            targetType = targetType,
            targetId = targetId,
            previousValue = prevValue,
            newValue = newValue,
            reason = reason,
            timestamp = System.currentTimeMillis()
        )
        _auditLogs.update { listOf(log) + it }
    }
}

data class QuizCompletionResult(
    val isCorrect: Boolean,
    val correctIndex: Int,
    val currentCycleProgress: Int,
    val requiredCycleQuizzes: Int,
    val isRewardCycleReady: Boolean
)
