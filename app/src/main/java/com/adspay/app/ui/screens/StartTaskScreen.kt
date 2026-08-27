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
import com.adspay.app.data.models.Quiz
import com.adspay.app.data.models.User
import com.adspay.app.data.repository.AdsPayRepository
import com.adspay.app.ui.components.CountdownTimerView
import com.adspay.app.ui.components.StartIoBannerComposable
import com.adspay.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

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
    val adError by StartIoAdManager.adErrorMessage.collectAsState()

    val requiredCycleCount = appSettings.rewardCycleQuizzesCount

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

    // Pre-load Rewarded Ad when user is near completion
    LaunchedEffect(user.currentCycleQuizzes) {
        if (user.currentCycleQuizzes >= requiredCycleCount - 1 && appSettings.isRewardedAdsEnabled) {
            StartIoAdManager.loadRewardedAd(context)
        }
    }

    val activeQuizzes = remember(quizzes) { quizzes.filter { it.isActive } }
    val currentQuiz = activeQuizzes.getOrNull(currentQuizIndex % activeQuizzes.size.coerceAtLeast(1))

    // Start Attempt and Countdown Timer
    fun setupNextQuiz() {
        if (currentQuiz != null) {
            selectedOptionIndex = -1
            isAnswerSubmitted = false
            isOptionCorrect = false
            timerSecondsRemaining = currentQuiz.timerSeconds
            isTimerFinished = false
            statusMessage = null
            isErrorMessage = false

            val attemptRes = AdsPayRepository.startTaskAttempt(currentQuiz.id)
            attemptRes.onSuccess {
                currentAttemptId = it.id
            }.onFailure {
                statusMessage = it.message
                isErrorMessage = true
            }
        }
    }

    LaunchedEffect(currentQuizIndex) {
        setupNextQuiz()
    }

    // Active 10-second countdown
    LaunchedEffect(currentQuizIndex, isTimerFinished) {
        while (timerSecondsRemaining > 0 && !isTimerFinished) {
            delay(1000L)
            timerSecondsRemaining -= 1
        }
        if (timerSecondsRemaining <= 0) {
            isTimerFinished = true
        }
    }

    // Rewarded Ad completion trigger
    fun triggerRewardedAdFlow() {
        if (!appSettings.isRewardedAdsEnabled) {
            // If rewarded ads are disabled by admin, credit directly
            val res = AdsPayRepository.verifyAndClaimRewardedAd()
            res.onSuccess {
                rewardCelebrationPoints = it
            }
            return
        }

        if (activity == null) return

        isShowingAdDialog = true

        StartIoAdManager.showRewardedAd(
            activity = activity,
            onVideoCompleted = {
                // Server verified reward crediting
                val res = AdsPayRepository.verifyAndClaimRewardedAd()
                res.onSuccess { points ->
                    rewardCelebrationPoints = points
                    isShowingAdDialog = false
                }.onFailure { err ->
                    statusMessage = err.message
                    isErrorMessage = true
                    isShowingAdDialog = false
                }
            },
            onAdClosedWithoutCompletion = {
                statusMessage = "You must watch the full rewarded video ad to receive your 1 point."
                isErrorMessage = true
                isShowingAdDialog = false
            },
            onFailedToShow = { msg ->
                statusMessage = "Ad is loading: $msg. Please try watching again in a few seconds."
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
                            text = "Cycle Progress: ${user.currentCycleQuizzes}/$requiredCycleCount",
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
                                    text = "Reward Cycle Goal",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "$requiredCycleCount Valid Quizzes ➔ 1 Rewarded Ad = +${appSettings.rewardPointsPerCycle} Point",
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

                        val progressFraction = (user.currentCycleQuizzes.toFloat() / requiredCycleCount.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (progressFraction >= 1f) GoldAccent else PurplePrimary,
                            trackColor = PurpleSubtle
                        )
                    }
                }

                // If user reached the 5 quizzes threshold, show Rewarded Ad Claim Box!
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
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(54.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Cycle Completed! 🎉",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "You have answered $requiredCycleCount quizzes! Watch the official Start.io Rewarded Video Ad to claim your +${appSettings.rewardPointsPerCycle} Point reward.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { triggerRewardedAdFlow() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = TextPrimary)
                            ) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Watch Rewarded Ad (+${appSettings.rewardPointsPerCycle} Pt)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                } else if (currentQuiz != null) {
                    // Active Quiz Screen
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
                            // Top Quiz Bar: Category + 10s Timer
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
                                        text = "Quiz #${(user.completedQuizzesCount + 1)} • ${currentQuiz.category}",
                                        color = PurplePrimary,
                                        fontSize = 12.sp,
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
                                        val attemptId = currentAttemptId
                                        if (attemptId == null) {
                                            statusMessage = "Starting attempt session..."
                                            return@Button
                                        }
                                        if (selectedOptionIndex == -1) {
                                            statusMessage = "Please select an answer option."
                                            isErrorMessage = true
                                            return@Button
                                        }
                                        if (!isTimerFinished) {
                                            statusMessage = "Anti-fraud rule: You must remain on the quiz for the full 10-second countdown."
                                            isErrorMessage = true
                                            return@Button
                                        }

                                        val completeRes = AdsPayRepository.completeQuiz(attemptId, selectedOptionIndex)
                                        completeRes.onSuccess { result ->
                                            isAnswerSubmitted = true
                                            isOptionCorrect = result.isCorrect
                                            if (result.isCorrect) {
                                                statusMessage = "Correct answer! (${result.currentCycleProgress}/${result.requiredCycleQuizzes} quizzes completed)."
                                                isErrorMessage = false
                                            } else {
                                                statusMessage = "Incorrect! The right answer was option ${('A' + result.correctIndex)}."
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
                                        text = if (user.currentCycleQuizzes >= requiredCycleCount) "Proceed to Rewarded Ad ➔" else "Next Quiz ➔",
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
                            text = "Reward Claimed! 💎",
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
                                text = "+${pts} Points Added",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenSuccess
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Start.io rewarded ad successfully verified! Your new point balance is ${String.format("%.1f", user.points)} points.",
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
                            Text("Start Next Cycle")
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
