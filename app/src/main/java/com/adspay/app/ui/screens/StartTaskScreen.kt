package com.adspay.app.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adspay.app.ads.RewardedAdStatus
import com.adspay.app.ads.StartIoAdManager
import com.adspay.app.data.NetworkUtils
import com.adspay.app.data.models.Quiz
import com.adspay.app.data.models.User
import com.adspay.app.data.repository.AdsPayRepository
import com.adspay.app.ui.components.CountdownTimerView
import com.adspay.app.ui.components.StartIoBannerComposable
import com.adspay.app.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartTaskScreen(
    user: User,
    onNavigateBack: () -> Unit,
    onNavigateToWallet: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val appSettings by AdsPayRepository.appSettings.collectAsState()
    val quizzes by AdsPayRepository.quizzes.collectAsState()
    val adStatus by StartIoAdManager.rewardedAdStatus.collectAsState()

    val requiredCycleCount = appSettings.rewardCycleQuizzesCount // 5

    // Local Quiz Engine State
    var currentQuizIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableIntStateOf(-1) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var isOptionCorrect by remember { mutableStateOf(false) }

    var timerSecondsRemaining by remember { mutableIntStateOf(appSettings.quizTimerSeconds) }
    var isTimerFinished by remember { mutableStateOf(false) }
    var currentAttemptId by remember { mutableStateOf<String?>(null) }

    var rewardCelebrationPoints by remember { mutableStateOf<Double?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorMessage by remember { mutableStateOf(false) }
    var isShowingAdDialog by remember { mutableStateOf(false) }

    // Proactively pre-load Rewarded Ad when user is on task screen
    LaunchedEffect(user.currentCycleQuizzes) {
        if (appSettings.isRewardedAdsEnabled) {
            StartIoAdManager.loadRewardedAd(context)
        }
    }

    val activeQuizzes = remember(quizzes) { quizzes.filter { it.isActive } }
    val currentQuiz = activeQuizzes.getOrNull(currentQuizIndex % activeQuizzes.size.coerceAtLeast(1))

    fun setupNextQuiz() {
        if (currentQuiz != null) {
            selectedOptionIndex = -1
            isAnswerSubmitted = false
            isOptionCorrect = false
            timerSecondsRemaining = currentQuiz.timerSeconds
            isTimerFinished = false
            statusMessage = null
            isErrorMessage = false

            if (!NetworkUtils.isInternetAvailable(context)) {
                statusMessage = NetworkUtils.ERROR_NO_INTERNET
                isErrorMessage = true
                return
            }

            val attemptRes = AdsPayRepository.startTaskAttempt(currentQuiz.id)
            attemptRes.onSuccess {
                currentAttemptId = it.id
            }.onFailure {
                statusMessage = it.message
                isErrorMessage = true
            }
        }
    }

    LaunchedEffect(currentQuizIndex, user.currentCycleQuizzes) {
        if (user.currentCycleQuizzes < requiredCycleCount) {
            setupNextQuiz()
        }
    }

    // Active 10-second countdown
    LaunchedEffect(currentQuizIndex, isTimerFinished, user.currentCycleQuizzes) {
        if (user.currentCycleQuizzes < requiredCycleCount) {
            while (timerSecondsRemaining > 0 && !isTimerFinished) {
                delay(1000L)
                timerSecondsRemaining -= 1
            }
            if (timerSecondsRemaining <= 0) {
                isTimerFinished = true
            }
        }
    }

    // Rewarded Ad completion trigger
    fun triggerRewardedAdFlow() {
        if (!NetworkUtils.isInternetAvailable(context)) {
            statusMessage = NetworkUtils.ERROR_NO_INTERNET
            isErrorMessage = true
            return
        }

        if (!appSettings.isRewardedAdsEnabled) {
            // Admin fallback if video ads are disabled
            val res = AdsPayRepository.verifyAndClaimRewardedAd()
            res.onSuccess {
                rewardCelebrationPoints = it
            }.onFailure {
                statusMessage = it.message
                isErrorMessage = true
            }
            return
        }

        if (activity == null) {
            statusMessage = "Could not attach ad window. Please reopen the screen."
            isErrorMessage = true
            return
        }

        isShowingAdDialog = true
        statusMessage = null

        StartIoAdManager.showRewardedAd(
            activity = activity,
            onVideoCompleted = {
                // Strictly verified Start.io completion callback -> Add points and reset cycle to 0/5
                val res = AdsPayRepository.verifyAndClaimRewardedAd()
                res.onSuccess { points ->
                    rewardCelebrationPoints = points
                    isShowingAdDialog = false
                    currentQuizIndex = 0
                }.onFailure { err ->
                    statusMessage = err.message
                    isErrorMessage = true
                    isShowingAdDialog = false
                }
            },
            onAdClosedWithoutCompletion = {
                statusMessage = "You must watch the full rewarded video ad to receive your 1 point reward."
                isErrorMessage = true
                isShowingAdDialog = false
            },
            onFailedToShow = { msg ->
                statusMessage = "Ad loading: $msg. Please tap Watch Ad again in a few seconds."
                isErrorMessage = true
                isShowingAdDialog = false
                StartIoAdManager.loadRewardedAd(context)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Daily Quiz Tasks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SurfaceWhite
                        )
                        Text(
                            text = "Cycle: ${user.currentCycleQuizzes}/$requiredCycleCount Quizzes",
                            style = MaterialTheme.typography.bodySmall,
                            color = PurpleLighter
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SurfaceWhite)
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite.copy(alpha = 0.2f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Stars, null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format("%.1f", user.points)} pts",
                                color = SurfaceWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PurpleDark,
                    titleContentColor = SurfaceWhite
                )
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                // Reward Cycle Tracker Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(4.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "5-Quiz Reward Cycle",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Complete 5 Quizzes ➔ Watch Rewarded Ad ➔ Earn +${appSettings.rewardPointsPerCycle} Point",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (user.currentCycleQuizzes >= requiredCycleCount) GoldLight else PurpleSubtle
                            ) {
                                Text(
                                    text = "${user.currentCycleQuizzes} / $requiredCycleCount",
                                    color = if (user.currentCycleQuizzes >= requiredCycleCount) GoldAccent else PurplePrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Cycle Visual Steps Indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (step in 1..5) {
                                val isStepDone = user.currentCycleQuizzes >= step
                                val isCurrentStep = user.currentCycleQuizzes == (step - 1)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 2.dp)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when {
                                                isStepDone -> GoldAccent
                                                isCurrentStep -> PurplePrimary
                                                else -> PurpleSubtle
                                            }
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Start (0/5)", fontSize = 10.sp, color = TextMuted)
                            Text("Quiz 3 (3/5)", fontSize = 10.sp, color = TextMuted)
                            Text("Rewarded Ad (5/5)", fontSize = 10.sp, color = if (user.currentCycleQuizzes >= 5) GoldAccent else TextMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // If user reached the 5 quizzes threshold (5/5), show Rewarded Ad Claim Box!
                if (user.currentCycleQuizzes >= requiredCycleCount) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .shadow(6.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PurpleSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = GoldLight,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "5 of 5 Quizzes Completed! 🎉",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Great job! Watch the official Start.io Rewarded Video Ad to verify your cycle and claim your +${appSettings.rewardPointsPerCycle} Point reward.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            val isAdActionInProgress = isShowingAdDialog || adStatus == RewardedAdStatus.LOADING || adStatus == RewardedAdStatus.SHOWING

                            Button(
                                onClick = { triggerRewardedAdFlow() },
                                enabled = !isAdActionInProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldAccent,
                                    contentColor = TextPrimary,
                                    disabledContainerColor = GoldAccent.copy(alpha = 0.5f),
                                    disabledContentColor = TextPrimary.copy(alpha = 0.7f)
                                )
                            ) {
                                if (isAdActionInProgress) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = TextPrimary,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Loading Video Ad...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                } else {
                                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Watch Rewarded Ad (+${appSettings.rewardPointsPerCycle} Point)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                } else if (currentQuiz != null) {
                    // Active Quiz Screen (Quiz 1 to 5)
                    val activeQuizStep = user.currentCycleQuizzes + 1 // 1, 2, 3, 4, 5

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .shadow(4.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Top Quiz Bar: Step Number + 10s Timer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PurpleSubtle
                                ) {
                                    Text(
                                        text = "Quiz $activeQuizStep of 5 • ${currentQuiz.category}",
                                        color = PurplePrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                CountdownTimerView(
                                    remainingSeconds = timerSecondsRemaining,
                                    totalSeconds = currentQuiz.timerSeconds
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Question Text
                            Text(
                                text = currentQuiz.question,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                lineHeight = 24.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // 4 Options
                            currentQuiz.options.forEachIndexed { index, option ->
                                val isSelected = selectedOptionIndex == index
                                val isCorrectOption = index == currentQuiz.correctOptionIndex

                                val optionBorderColor = when {
                                    isAnswerSubmitted && isCorrectOption -> GreenSuccess
                                    isAnswerSubmitted && isSelected && !isOptionCorrect -> RedError
                                    isSelected -> PurplePrimary
                                    else -> BorderLight
                                }

                                val optionBgColor = when {
                                    isAnswerSubmitted && isCorrectOption -> GreenLight
                                    isAnswerSubmitted && isSelected && !isOptionCorrect -> RedLight
                                    isSelected -> PurpleSubtle
                                    else -> SurfaceWhite
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.5.dp, optionBorderColor, RoundedCornerShape(12.dp))
                                        .clickable(enabled = !isAnswerSubmitted) {
                                            selectedOptionIndex = index
                                        },
                                    color = optionBgColor,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) PurplePrimary else PurpleLighter,
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = ('A' + index).toString(),
                                                    color = if (isSelected) SurfaceWhite else PurpleDark,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Text(
                                            text = option,
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (isAnswerSubmitted && isCorrectOption) {
                                            Icon(Icons.Default.CheckCircle, null, tint = GreenSuccess, modifier = Modifier.size(20.dp))
                                        } else if (isAnswerSubmitted && isSelected && !isOptionCorrect) {
                                            Icon(Icons.Default.Cancel, null, tint = RedError, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Submit & Next Buttons
                            if (!isAnswerSubmitted) {
                                Button(
                                    onClick = {
                                        if (!NetworkUtils.isInternetAvailable(context)) {
                                            statusMessage = NetworkUtils.ERROR_NO_INTERNET
                                            isErrorMessage = true
                                            return@Button
                                        }

                                        val attemptId = currentAttemptId
                                        if (attemptId == null) {
                                            statusMessage = "Initializing task attempt..."
                                            isErrorMessage = true
                                            return@Button
                                        }
                                        if (selectedOptionIndex == -1) {
                                            statusMessage = "Please select an option before submitting."
                                            isErrorMessage = true
                                            return@Button
                                        }
                                        if (!isTimerFinished) {
                                            statusMessage = "Anti-fraud rule: Please wait for the 10-second timer to finish."
                                            isErrorMessage = true
                                            return@Button
                                        }

                                        val completeRes = AdsPayRepository.completeQuiz(attemptId, selectedOptionIndex)
                                        completeRes.onSuccess { result ->
                                            isAnswerSubmitted = true
                                            isOptionCorrect = result.isCorrect
                                            if (result.isCorrect) {
                                                statusMessage = "Correct! (${result.currentCycleProgress}/$requiredCycleCount quizzes completed in this cycle)."
                                                isErrorMessage = false
                                            } else {
                                                statusMessage = "Incorrect. The right option was ${('A' + result.correctIndex)}. (${result.currentCycleProgress}/$requiredCycleCount completed)."
                                                isErrorMessage = true
                                            }
                                        }.onFailure { err ->
                                            statusMessage = err.message
                                            isErrorMessage = true
                                        }
                                    },
                                    enabled = selectedOptionIndex != -1 && isTimerFinished,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                                ) {
                                    Text(
                                        text = if (!isTimerFinished) "Wait for Timer (${timerSecondsRemaining}s)" else "Submit Answer",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        currentQuizIndex += 1
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PurpleDark)
                                ) {
                                    Text(
                                        text = if (user.currentCycleQuizzes >= requiredCycleCount) "Proceed to Rewarded Ad (5/5) ➔" else "Next Quiz (${user.currentCycleQuizzes + 1}/5) ➔",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SurfaceWhite
                                    )
                                }
                            }
                        }
                    }
                }

                // Status message alert
                statusMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isErrorMessage) RedLight else GreenLight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isErrorMessage) Icons.Default.Info else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isErrorMessage) RedError else GreenSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = if (isErrorMessage) RedError else GreenSuccess,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Banner Ad on Quiz Screen
                if (appSettings.isBannerAdsEnabled) {
                    StartIoBannerComposable(isEnabled = true)
                }
            }

            // Reward Celebration Modal Dialog
            rewardCelebrationPoints?.let { pts ->
                AlertDialog(
                    onDismissRequest = {
                        rewardCelebrationPoints = null
                        setupNextQuiz()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Reward Verified! 💎",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "+${pts} Point Added",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenSuccess
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Start.io rewarded video ad completed! Cycle reset to 0/5. Your new balance is ${String.format("%.1f", user.points)} points.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                rewardCelebrationPoints = null
                                setupNextQuiz()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Text("Start New Cycle (Quiz 1/5)")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = {
                            rewardCelebrationPoints = null
                            onNavigateToWallet()
                        }) {
                            Text("Cash Out")
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = SurfaceWhite
                )
            }
        }
    }
}
