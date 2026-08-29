package com.adspay.app.data.api

import com.adspay.app.data.models.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object AdsPayApiClient {
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    // --- Authentication ---

    fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
        referralCode: String?
    ): Result<User> {
        return try {
            val jsonBody = JSONObject().apply {
                put("name", name.trim())
                put("email", email.trim().lowercase())
                put("phone", phone.trim())
                put("password", password)
                if (!referralCode.isNullOrBlank()) {
                    put("referralCode", referralCode.trim().uppercase())
                }
            }

            val request = Request.Builder()
                .url(ApiConfig.buildUrl("/api/app/auth/register"))
                .post(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = extractErrorMessage(responseBody, "Registration failed (HTTP ${response.code})")
                return Result.failure(Exception(errorMsg))
            }

            val trimmed = responseBody.trim()
            if (!trimmed.startsWith("{")) {
                return Result.failure(Exception("Invalid server response. Please verify backend API status."))
            }

            val resJson = JSONObject(trimmed)
            if (resJson.has("user")) {
                val userObj = resJson.getJSONObject("user")
                val user = parseUserJson(userObj)
                Result.success(user)
            } else if (resJson.has("error")) {
                Result.failure(Exception(resJson.getString("error")))
            } else if (resJson.has("message")) {
                Result.failure(Exception(resJson.getString("message")))
            } else {
                Result.failure(Exception("Registration response did not contain user data."))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Unable to reach server. Please check your internet connection."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Registration error occurred."))
        }
    }

    fun login(emailOrPhone: String, password: String): Result<User> {
        return try {
            val jsonBody = JSONObject().apply {
                put("emailOrPhone", emailOrPhone.trim())
                put("password", password)
            }

            val request = Request.Builder()
                .url(ApiConfig.buildUrl("/api/app/auth/login"))
                .post(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = extractErrorMessage(responseBody, "Login failed (HTTP ${response.code})")
                return Result.failure(Exception(errorMsg))
            }

            val trimmed = responseBody.trim()
            if (!trimmed.startsWith("{")) {
                return Result.failure(Exception("Invalid server response. Please verify backend API status."))
            }

            val resJson = JSONObject(trimmed)
            if (resJson.has("user")) {
                val userObj = resJson.getJSONObject("user")
                val user = parseUserJson(userObj)
                Result.success(user)
            } else if (resJson.has("error")) {
                Result.failure(Exception(resJson.getString("error")))
            } else if (resJson.has("message")) {
                Result.failure(Exception(resJson.getString("message")))
            } else {
                Result.failure(Exception("Login response did not contain user data."))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Unable to reach server. Please check your internet connection."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Login error occurred."))
        }
    }

    fun fetchUserProfile(userId: String): Result<User> {
        return try {
            val request = Request.Builder()
                .url(ApiConfig.buildUrl("/api/app/user/profile?userId=${userId.trim()}"))
                .get()
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return Result.failure(Exception("Failed to fetch profile"))
            }

            val resJson = JSONObject(responseBody)
            val userObj = resJson.getJSONObject("user")
            val user = parseUserJson(userObj)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- App Settings & Sync ---

    fun fetchSettings(): Result<AppSettings> {
        return try {
            val request = Request.Builder()
                .url(ApiConfig.buildUrl("/api/app/settings"))
                .get()
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return Result.failure(Exception("Failed to fetch settings"))
            }

            val obj = JSONObject(responseBody)
            val settings = AppSettings(
                rewardCycleQuizzesCount = obj.optInt("rewardCycleQuizzesCount", 5),
                quizTimerSeconds = obj.optInt("quizTimerSeconds", 10),
                rewardPointsPerCycle = obj.optDouble("rewardPointsPerCycle", 1.0),
                pointMonetaryValue = obj.optDouble("pointMonetaryValue", 0.20),
                currencySymbol = obj.optString("currencySymbol", "৳"),
                referralCommissionPercent = obj.optDouble("referralCommissionPercent", 10.0),
                minWithdrawalPoints = obj.optDouble("minWithdrawalPoints", 50.0),
                maxWithdrawalPoints = obj.optDouble("maxWithdrawalPoints", 10000.0),
                dailyTaskLimit = obj.optInt("dailyTaskLimit", 100),
                hourlyTaskLimit = obj.optInt("hourlyTaskLimit", 20),
                isRegistrationEnabled = obj.optBoolean("isRegistrationEnabled", true),
                isLoginEnabled = obj.optBoolean("isLoginEnabled", true),
                isTaskSystemEnabled = obj.optBoolean("isTaskSystemEnabled", true),
                isBannerAdsEnabled = obj.optBoolean("isBannerAdsEnabled", true),
                isRewardedAdsEnabled = obj.optBoolean("isRewardedAdsEnabled", true),
                isReferralEnabled = obj.optBoolean("isReferralEnabled", true),
                isWithdrawEnabled = obj.optBoolean("isWithdrawEnabled", true),
                isBkashEnabled = obj.optBoolean("isBkashEnabled", true),
                isNagadEnabled = obj.optBoolean("isNagadEnabled", true),
                isUsdtEnabled = obj.optBoolean("isUsdtEnabled", true),
                isLeaderboardEnabled = obj.optBoolean("isLeaderboardEnabled", true),
                isNotificationsEnabled = obj.optBoolean("isNotificationsEnabled", true),
                isMaintenanceMode = obj.optBoolean("isMaintenanceMode", false),
                maintenanceMessage = obj.optString("maintenanceMessage", "Scheduled system upgrade."),
                appName = obj.optString("appName", "Ads Pay"),
                startIoAppId = obj.optString("startIoAppId", "207226080"),
                announcementText = obj.optString("announcementText", ""),
                telegramUrl = obj.optString("telegramUrl", "https://t.me/adspayofficial"),
                youtubeUrl = obj.optString("youtubeUrl", "https://youtube.com/@adspayofficial"),
                supportContact = obj.optString("supportContact", "support@adspay.app"),
                aboutText = obj.optString("aboutText", ""),
                howToWorkText = obj.optString("howToWorkText", "")
            )
            Result.success(settings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Task & Quiz Management ---

    fun startTask(userId: String, quizId: String): Result<TaskAttempt> {
        return try {
            val jsonBody = JSONObject().apply {
                put("userId", userId)
                put("quizId", quizId)
            }

            val request = Request.Builder()
                .url(ApiConfig.buildUrl("/api/app/task/start"))
                .post(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = extractErrorMessage(responseBody, "Could not start task")
                return Result.failure(Exception(errorMsg))
            }

            val resJson = JSONObject(responseBody)
            val attObj = resJson.getJSONObject("attempt")
            val attempt = TaskAttempt(
                id = attObj.optString("id", ""),
                userId = attObj.optString("userId", userId),
                quizId = attObj.optString("quizId", quizId),
                startTime = attObj.optLong("startTime", System.currentTimeMillis()),
                cycleIndex = attObj.optInt("cycleIndex", 1),
                token = attObj.optString("token", "")
            )
            Result.success(attempt)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun completeTask(attemptId: String, selectedOptionIndex: Int): Result<QuizCompletionResult> {
        return try {
            val jsonBody = JSONObject().apply {
                put("attemptId", attemptId)
                put("selectedOptionIndex", selectedOptionIndex)
            }

            val request = Request.Builder()
                .url(ApiConfig.buildUrl("/api/app/task/complete"))
                .post(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = extractErrorMessage(responseBody, "Task completion failed")
                return Result.failure(Exception(errorMsg))
            }

            val resJson = JSONObject(responseBody)
            val result = QuizCompletionResult(
                isCorrect = resJson.optBoolean("isCorrect", false),
                correctIndex = resJson.optInt("correctIndex", 0),
                currentCycleProgress = resJson.optInt("currentCycleProgress", 1),
                requiredCycleQuizzes = resJson.optInt("requiredCycleQuizzes", 5),
                isRewardCycleReady = resJson.optBoolean("isRewardCycleReady", false)
            )
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun claimReward(userId: String): Result<Double> {
        return try {
            val jsonBody = JSONObject().apply {
                put("userId", userId)
            }

            val request = Request.Builder()
                .url(ApiConfig.buildUrl("/api/app/task/claim-reward"))
                .post(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = extractErrorMessage(responseBody, "Failed to claim reward")
                return Result.failure(Exception(errorMsg))
            }

            val resJson = JSONObject(responseBody)
            val rewardPoints = resJson.optDouble("rewardPoints", 1.0)
            Result.success(rewardPoints)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Withdrawal ---

    fun submitWithdrawal(
        userId: String,
        method: WithdrawMethod,
        points: Double,
        accountInfo: String,
        accountHolderName: String
    ): Result<WithdrawalRequest> {
        return try {
            val jsonBody = JSONObject().apply {
                put("userId", userId)
                put("method", method.name)
                put("points", points)
                put("accountInfo", accountInfo.trim())
                put("accountHolderName", accountHolderName.trim())
            }

            val request = Request.Builder()
                .url(ApiConfig.buildUrl("/api/app/withdraw"))
                .post(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = extractErrorMessage(responseBody, "Withdrawal request failed")
                return Result.failure(Exception(errorMsg))
            }

            val resJson = JSONObject(responseBody)
            val wthObj = resJson.getJSONObject("withdrawal")
            val item = parseWithdrawalJson(wthObj)
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Helpers & Parsing ---

    private fun extractErrorMessage(responseBody: String, defaultMsg: String): String {
        return try {
            val trimmed = responseBody.trim()
            if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                if (obj.has("error")) obj.getString("error")
                else if (obj.has("message")) obj.getString("message")
                else defaultMsg
            } else if (trimmed.startsWith("<") || trimmed.contains("<!doctype", ignoreCase = true) || trimmed.contains("<html", ignoreCase = true)) {
                "Backend server returned an HTML error page instead of JSON. Please verify backend server route and availability."
            } else {
                if (trimmed.isNotBlank() && trimmed.length < 150) trimmed else defaultMsg
            }
        } catch (e: Exception) {
            defaultMsg
        }
    }

    private fun parseUserJson(obj: JSONObject): User {
        return User(
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
            role = if (obj.optString("role") == "ADMIN") UserRole.ADMIN else UserRole.USER,
            isBlocked = obj.optBoolean("isBlocked", false),
            isTaskDisabled = obj.optBoolean("isTaskDisabled", false),
            isWithdrawDisabled = obj.optBoolean("isWithdrawDisabled", false),
            isReferralDisabled = obj.optBoolean("isReferralDisabled", false),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            lastActiveAt = obj.optLong("lastActiveAt", System.currentTimeMillis())
        )
    }

    private fun parseWithdrawalJson(obj: JSONObject): WithdrawalRequest {
        val methodStr = obj.optString("method", "BKASH")
        val method = when (methodStr) {
            "NAGAD" -> WithdrawMethod.NAGAD
            "USDT_BEP20" -> WithdrawMethod.USDT_BEP20
            else -> WithdrawMethod.BKASH
        }

        val statusStr = obj.optString("status", "PENDING")
        val status = when (statusStr) {
            "PROCESSING" -> WithdrawalStatus.PROCESSING
            "APPROVED" -> WithdrawalStatus.APPROVED
            "PAID" -> WithdrawalStatus.PAID
            "REJECTED" -> WithdrawalStatus.REJECTED
            "CANCELLED" -> WithdrawalStatus.CANCELLED
            else -> WithdrawalStatus.PENDING
        }

        return WithdrawalRequest(
            id = obj.optString("id", ""),
            userId = obj.optString("userId", ""),
            userName = obj.optString("userName", ""),
            userEmail = obj.optString("userEmail", ""),
            points = obj.optDouble("points", 0.0),
            amountCurrency = obj.optDouble("amountCurrency", 0.0),
            currencySymbol = obj.optString("currencySymbol", "৳"),
            method = method,
            accountInfo = obj.optString("accountInfo", ""),
            accountHolderName = obj.optString("accountHolderName", ""),
            status = status,
            requestDate = obj.optLong("requestDate", System.currentTimeMillis()),
            processedDate = if (obj.has("processedDate") && !obj.isNull("processedDate")) obj.getLong("processedDate") else null,
            adminNote = obj.optString("adminNote", "")
        )
    }
}
