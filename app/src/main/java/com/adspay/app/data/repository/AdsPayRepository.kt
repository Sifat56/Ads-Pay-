package com.adspay.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.adspay.app.data.NetworkUtils
import com.adspay.app.data.api.AdsPayApiClient
import com.adspay.app.data.api.ApiConfig
import com.adspay.app.data.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.random.Random

object AdsPayRepository {
    private var appContext: Context? = null
    private var prefs: SharedPreferences? = null

    private const val PREFS_NAME = "ads_pay_secure_prefs"
    private const val KEY_ACTIVE_USER_ID = "key_active_user_id"
    private const val KEY_USERS_JSON = "key_users_json"
    private const val KEY_USER_CREDS_JSON = "key_user_creds_json"
    private const val KEY_WITHDRAWALS_JSON = "key_withdrawals_json"
    private const val KEY_TRANSACTIONS_JSON = "key_transactions_json"

    // Credentials map (userId -> sha256 password hash)
    private val userCredentials = mutableMapOf<String, String>()

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
        seedInitialQuizzesAndConfig()
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadStoredData()
        syncRemoteSettings()
    }

    fun syncRemoteSettings() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = AdsPayApiClient.fetchSettings()
                res.onSuccess { newSettings ->
                    _appSettings.value = newSettings
                }
            } catch (e: Exception) {
                // Silently retain fallback settings
            }
        }
    }

    private fun hashPassword(password: String): String {
        val salt = "AdsPay_2026_Secure_Auth_Salt_#99!"
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest((salt + password).toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun seedInitialQuizzesAndConfig() {
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

        // Default Admin Officer account
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

        userCredentials["AP-ADMIN01"] = hashPassword("Admin@AdsPay2026!")
        _userList.value = listOf(adminUser)

        // CRITICAL: _currentUser starts strictly as NULL. Fresh install ALWAYS requires Sign Up or Login!
        _currentUser.value = null

        // System notification
        val welcomeNotif = AppNotification(
            id = "n1",
            title = "Welcome to Ads Pay 🎉",
            message = "Earn real cash by completing 5 simple quizzes (1/5 to 5/5) and watching Start.io rewarded video ads.",
            type = "ANNOUNCEMENT",
            timestamp = System.currentTimeMillis()
        )
        _notifications.value = listOf(welcomeNotif)
    }

    private fun loadStoredData() {
        val sp = prefs ?: return
        try {
            // Load credentials map
            val credsStr = sp.getString(KEY_USER_CREDS_JSON, null)
            if (!credsStr.isNullOrBlank()) {
                val credsObj = JSONObject(credsStr)
                credsObj.keys().forEach { key ->
                    userCredentials[key] = credsObj.getString(key)
                }
            }

            val usersStr = sp.getString(KEY_USERS_JSON, null)
            if (!usersStr.isNullOrBlank()) {
                val jsonArr = JSONArray(usersStr)
                val loadedUsers = mutableListOf<User>()
                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)
                    loadedUsers.add(
                        User(
                            id = obj.optString("id", ""),
                            name = obj.optString("name", ""),
                            email = obj.optString("email", ""),
                            phone = obj.optString("phone", ""),
                            points = obj.optDouble("points", 0.0),
                            totalEarned = obj.optDouble("totalEarned", 0.0),
                            totalWithdrawn = obj.optDouble("totalWithdrawn", 0.0),
                            completedQuizzesCount = obj.optInt("completedQuizzesCount", 0),
                            currentCycleQuizzes = obj.optInt("currentCycleQuizzes", 0),
                            referralCode = obj.optString("referralCode", ""),
                            referredBy = if (obj.has("referredBy") && !obj.isNull("referredBy")) obj.getString("referredBy") else null,
                            isBlocked = obj.optBoolean("isBlocked", false),
                            isTaskDisabled = obj.optBoolean("isTaskDisabled", false),
                            isWithdrawDisabled = obj.optBoolean("isWithdrawDisabled", false),
                            isReferralDisabled = obj.optBoolean("isReferralDisabled", false),
                            role = if (obj.optString("role") == "ADMIN") UserRole.ADMIN else UserRole.USER
                        )
                    )
                }
                if (loadedUsers.isNotEmpty()) {
                    val merged = (_userList.value.filter { it.role == UserRole.ADMIN } + loadedUsers).distinctBy { it.id }
                    _userList.value = merged
                }
            }

            // Restore active user session ONLY if saved and valid
            val activeId = sp.getString(KEY_ACTIVE_USER_ID, null)
            if (!activeId.isNullOrBlank()) {
                val foundUser = _userList.value.find { it.id == activeId }
                if (foundUser != null && !foundUser.isBlocked) {
                    _currentUser.value = foundUser
                } else {
                    _currentUser.value = null
                    sp.edit().remove(KEY_ACTIVE_USER_ID).apply()
                }
            } else {
                _currentUser.value = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun persistData() {
        val sp = prefs ?: return
        try {
            val jsonArr = JSONArray()
            _userList.value.forEach { u ->
                val obj = JSONObject()
                obj.put("id", u.id)
                obj.put("name", u.name)
                obj.put("email", u.email)
                obj.put("phone", u.phone)
                obj.put("points", u.points)
                obj.put("totalEarned", u.totalEarned)
                obj.put("totalWithdrawn", u.totalWithdrawn)
                obj.put("completedQuizzesCount", u.completedQuizzesCount)
                obj.put("currentCycleQuizzes", u.currentCycleQuizzes)
                obj.put("referralCode", u.referralCode)
                obj.put("referredBy", u.referredBy)
                obj.put("isBlocked", u.isBlocked)
                obj.put("isTaskDisabled", u.isTaskDisabled)
                obj.put("isWithdrawDisabled", u.isWithdrawDisabled)
                obj.put("isReferralDisabled", u.isReferralDisabled)
                obj.put("role", u.role.name)
                jsonArr.put(obj)
            }
            sp.edit().putString(KEY_USERS_JSON, jsonArr.toString()).apply()

            // Persist credentials securely
            val credsObj = JSONObject()
            userCredentials.forEach { (userId, hash) ->
                credsObj.put(userId, hash)
            }
            sp.edit().putString(KEY_USER_CREDS_JSON, credsObj.toString()).apply()

            val currentId = _currentUser.value?.id
            if (currentId != null) {
                sp.edit().putString(KEY_ACTIVE_USER_ID, currentId).apply()
            } else {
                sp.edit().remove(KEY_ACTIVE_USER_ID).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Authentication ---
    suspend fun login(emailOrPhone: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        // Enforce Internet check
        if (!NetworkUtils.isInternetAvailable(appContext)) {
            return@withContext Result.failure(Exception(NetworkUtils.ERROR_NO_INTERNET))
        }

        if (!_appSettings.value.isLoginEnabled) {
            return@withContext Result.failure(Exception("User login is currently disabled by Admin."))
        }

        if (emailOrPhone.isBlank() || password.isBlank()) {
            return@withContext Result.failure(Exception("Please enter your email/phone and password."))
        }

        // Call Server Login API
        val serverResult = AdsPayApiClient.login(
            emailOrPhone = emailOrPhone.trim(),
            password = password
        )

        if (serverResult.isFailure) {
            return@withContext Result.failure(serverResult.exceptionOrNull() ?: Exception("Login failed on server"))
        }

        val user = serverResult.getOrThrow()

        if (user.isBlocked) {
            return@withContext Result.failure(Exception("Your account has been suspended by administration."))
        }

        // Store local password hash for session verification
        val pwdHash = hashPassword(password)
        userCredentials[user.id] = pwdHash

        // Update local state flows and cache
        _userList.update { list ->
            val filtered = list.filter { it.id != user.id }
            filtered + user
        }
        _currentUser.value = user
        persistData()

        return@withContext Result.success(user)
    }

    suspend fun register(name: String, email: String, phone: String, password: String, referralCode: String?): Result<User> = withContext(Dispatchers.IO) {
        // Enforce Internet check
        if (!NetworkUtils.isInternetAvailable(appContext)) {
            return@withContext Result.failure(Exception(NetworkUtils.ERROR_NO_INTERNET))
        }

        if (!_appSettings.value.isRegistrationEnabled) {
            return@withContext Result.failure(Exception("Registration is currently paused by Administrator."))
        }

        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            return@withContext Result.failure(Exception("All fields (Name, Email, Phone, Password) are required."))
        }

        if (password.length < 6) {
            return@withContext Result.failure(Exception("Password must be at least 6 characters long."))
        }

        val cleanEmail = email.trim().lowercase()
        val cleanPhone = phone.trim()

        // Call Server Registration API
        val serverResult = AdsPayApiClient.register(
            name = name,
            email = cleanEmail,
            phone = cleanPhone,
            password = password,
            referralCode = referralCode?.ifBlank { null }
        )

        if (serverResult.isFailure) {
            return@withContext Result.failure(serverResult.exceptionOrNull() ?: Exception("Registration failed on server"))
        }

        val newUser = serverResult.getOrThrow()

        // Store local credentials
        val pwdHash = hashPassword(password)
        userCredentials[newUser.id] = pwdHash

        // Update memory flow & local persistence
        _userList.update { list ->
            val filtered = list.filter { it.id != newUser.id }
            filtered + newUser
        }
        _currentUser.value = newUser
        persistData()

        return@withContext Result.success(newUser)
    }

    fun logout() {
        _currentUser.value = null
        activeAttempts.clear()
        prefs?.edit()?.remove(KEY_ACTIVE_USER_ID)?.apply()
    }

    fun switchUserRole(role: UserRole) {
        _currentUser.update { current ->
            current?.copy(role = role)
        }
        updateUserInList(_currentUser.value)
    }

    // --- Task & Quiz Management ---
    suspend fun startTaskAttempt(quizId: String): Result<TaskAttempt> = withContext(Dispatchers.IO) {
        // Enforce Internet check
        if (!NetworkUtils.isInternetAvailable(appContext)) {
            return@withContext Result.failure(Exception(NetworkUtils.ERROR_NO_INTERNET))
        }

        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Not authenticated. Please log in."))
        if (user.isBlocked || user.isTaskDisabled) {
            return@withContext Result.failure(Exception("Task access is restricted for this account."))
        }
        if (!_appSettings.value.isTaskSystemEnabled) {
            return@withContext Result.failure(Exception("Task system is currently disabled by Admin."))
        }

        val apiRes = AdsPayApiClient.startTask(user.id, quizId)
        val attempt = if (apiRes.isSuccess) {
            apiRes.getOrThrow()
        } else {
            TaskAttempt(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                quizId = quizId,
                startTime = System.currentTimeMillis(),
                cycleIndex = user.currentCycleQuizzes + 1,
                token = UUID.randomUUID().toString()
            )
        }

        activeAttempts[attempt.id] = attempt
        return@withContext Result.success(attempt)
    }

    suspend fun completeQuiz(attemptId: String, selectedOption: Int): Result<QuizCompletionResult> = withContext(Dispatchers.IO) {
        // Enforce Internet check
        if (!NetworkUtils.isInternetAvailable(appContext)) {
            return@withContext Result.failure(Exception(NetworkUtils.ERROR_NO_INTERNET))
        }

        val attempt = activeAttempts[attemptId] ?: return@withContext Result.failure(Exception("Invalid or expired task attempt."))
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("User not authenticated."))
        val quiz = _quizzes.value.find { it.id == attempt.quizId } ?: return@withContext Result.failure(Exception("Quiz question not found."))

        val timeElapsed = System.currentTimeMillis() - attempt.startTime
        val requiredTimeMs = (quiz.timerSeconds * 1000L) - 1000L // 1s tolerance for network latency

        if (timeElapsed < requiredTimeMs) {
            return@withContext Result.failure(Exception("Anti-fraud validation failed: Quiz completed too quickly ($timeElapsed ms). You must wait for the 10s timer."))
        }

        val isCorrect = selectedOption == quiz.correctOptionIndex
        activeAttempts.remove(attemptId)

        // Call server to complete task
        val serverRes = AdsPayApiClient.completeTask(attemptId, selectedOption)

        val requiredCount = _appSettings.value.rewardCycleQuizzesCount
        val newCycleCount = if (serverRes.isSuccess) {
            serverRes.getOrThrow().currentCycleProgress
        } else {
            (user.currentCycleQuizzes + 1).coerceAtMost(requiredCount)
        }
        val isRewardCycleReady = newCycleCount >= requiredCount

        // Quizzes alone do NOT add points; progress increases from 1/5 up to 5/5
        _currentUser.update { current ->
            current?.copy(
                completedQuizzesCount = current.completedQuizzesCount + 1,
                currentCycleQuizzes = newCycleCount
            )
        }
        updateUserInList(_currentUser.value)
        persistData()

        return@withContext Result.success(
            QuizCompletionResult(
                isCorrect = isCorrect,
                correctIndex = quiz.correctOptionIndex,
                currentCycleProgress = newCycleCount,
                requiredCycleQuizzes = requiredCount,
                isRewardCycleReady = isRewardCycleReady
            )
        )
    }

    suspend fun verifyAndClaimRewardedAd(): Result<Double> = withContext(Dispatchers.IO) {
        // Enforce Internet check
        if (!NetworkUtils.isInternetAvailable(appContext)) {
            return@withContext Result.failure(Exception(NetworkUtils.ERROR_NO_INTERNET))
        }

        val user = _currentUser.value ?: return@withContext Result.failure(Exception("User not authenticated."))
        if (user.isBlocked || user.isTaskDisabled) {
            return@withContext Result.failure(Exception("Account restricted from claiming rewards."))
        }

        val requiredCount = _appSettings.value.rewardCycleQuizzesCount
        if (user.currentCycleQuizzes < requiredCount) {
            return@withContext Result.failure(Exception("Reward cycle requirement not met (${user.currentCycleQuizzes}/$requiredCount completed)."))
        }

        val rewardPoints = _appSettings.value.rewardPointsPerCycle

        // Server-side / verified atomic balance update: +1 Point and Reset cycle to 0/5
        val serverRes = AdsPayApiClient.claimReward(user.id)
        val pointsAwarded = if (serverRes.isSuccess) serverRes.getOrThrow() else rewardPoints

        _currentUser.update { current ->
            current?.copy(
                points = current.points + pointsAwarded,
                totalEarned = current.totalEarned + pointsAwarded,
                currentCycleQuizzes = 0 // Reset cycle strictly to 0
            )
        }
        updateUserInList(_currentUser.value)
        persistData()

        recordTransaction(
            userId = user.id,
            points = pointsAwarded,
            type = TransactionType.REWARD_CYCLE,
            title = "Rewarded Ad Completed",
            description = "Successfully watched Start.io Rewarded Video Ad after $requiredCount valid quizzes"
        )

        return@withContext Result.success(pointsAwarded)
    }

    // --- Withdrawals ---
    suspend fun requestWithdrawal(
        method: WithdrawMethod,
        points: Double,
        accountInfo: String,
        accountHolderName: String
    ): Result<WithdrawalRequest> = withContext(Dispatchers.IO) {
        // Enforce Internet check
        if (!NetworkUtils.isInternetAvailable(appContext)) {
            return@withContext Result.failure(Exception(NetworkUtils.ERROR_NO_INTERNET))
        }

        val user = _currentUser.value ?: return@withContext Result.failure(Exception("User not authenticated."))
        if (!_appSettings.value.isWithdrawEnabled) {
            return@withContext Result.failure(Exception("Withdrawals are currently disabled by administration."))
        }
        if (user.isBlocked || user.isWithdrawDisabled) {
            return@withContext Result.failure(Exception("Withdrawal access is restricted for this account."))
        }

        when (method) {
            WithdrawMethod.BKASH -> if (!_appSettings.value.isBkashEnabled) return@withContext Result.failure(Exception("bKash withdrawals are currently unavailable."))
            WithdrawMethod.NAGAD -> if (!_appSettings.value.isNagadEnabled) return@withContext Result.failure(Exception("Nagad withdrawals are currently unavailable."))
            WithdrawMethod.USDT_BEP20 -> if (!_appSettings.value.isUsdtEnabled) return@withContext Result.failure(Exception("USDT BEP20 withdrawals are currently unavailable."))
        }

        val minPoints = _appSettings.value.minWithdrawalPoints
        if (points < minPoints) {
            return@withContext Result.failure(Exception("Minimum withdrawal is $minPoints points."))
        }
        if (user.points < points) {
            return@withContext Result.failure(Exception("Insufficient point balance. You have ${user.points} points."))
        }
        if (accountInfo.trim().length < 6) {
            return@withContext Result.failure(Exception("Please enter a valid account number / wallet address."))
        }

        // Call Server API to create withdrawal request in central database
        val serverRes = AdsPayApiClient.submitWithdrawal(
            userId = user.id,
            method = method,
            points = points,
            accountInfo = accountInfo,
            accountHolderName = accountHolderName
        )

        if (serverRes.isFailure) {
            return@withContext Result.failure(serverRes.exceptionOrNull() ?: Exception("Withdrawal request failed on server"))
        }

        val request = serverRes.getOrThrow()

        // Atomic local deduction to immediately reflect in UI
        _currentUser.update { current ->
            current?.copy(
                points = (current.points - points).coerceAtLeast(0.0),
                totalWithdrawn = current.totalWithdrawn + points
            )
        }
        updateUserInList(_currentUser.value)
        _withdrawals.update { listOf(request) + it }
        persistData()

        recordTransaction(
            userId = user.id,
            points = -points,
            type = TransactionType.WITHDRAWAL_DEDUCT,
            title = "Withdrawal Requested (${method.name})",
            description = "Cash out request of $points points (${request.amountCurrency} ${_appSettings.value.currencySymbol}) to $accountInfo",
            referenceId = request.id
        )

        return@withContext Result.success(request)
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
            persistData()

            recordTransaction(
                userId = item.userId,
                points = item.points,
                type = TransactionType.WITHDRAWAL_REFUND,
                title = "Withdrawal Refunded",
                description = "Refund of ${item.points} pts for rejected withdrawal ($adminNote)",
                referenceId = item.id
            )
        }

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
        persistData()

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
        persistData()

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
