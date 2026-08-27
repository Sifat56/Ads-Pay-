package com.adspay.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adspay.app.data.repository.AdsPayRepository
import com.adspay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToWorkScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val appSettings by AdsPayRepository.appSettings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How to Work & Rules", fontWeight = FontWeight.Bold, color = SurfaceWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SurfaceWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PurpleDark)
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PurpleSubtle)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(PurpleDarker, PurpleDark, PurplePrimary)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Simple 4-Step Earning Guide",
                                color = SurfaceWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Follow these official guidelines to earn smoothly and withdraw your money fast!",
                                color = PurpleLighter,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Steps
            item {
                StepCard(
                    stepNumber = "1",
                    title = "Start Daily Quizzes",
                    description = "Tap 'Start Task' on the home dashboard. Read the interactive question and select your answer.",
                    icon = Icons.Default.PlayCircle,
                    accentColor = PurplePrimary
                )
            }

            item {
                StepCard(
                    stepNumber = "2",
                    title = "Wait for 10-Second Timer",
                    description = "Anti-fraud protection requires you to stay on each quiz for the full 10 seconds. Submit your answer after the timer finishes.",
                    icon = Icons.Default.Timer,
                    accentColor = GoldAccent
                )
            }

            item {
                StepCard(
                    stepNumber = "3",
                    title = "Watch Rewarded Video Ad",
                    description = "After answering ${appSettings.rewardCycleQuizzesCount} valid quizzes consecutively, watch the full Start.io Rewarded Video Ad until completion.",
                    icon = Icons.Default.SmartDisplay,
                    accentColor = Color(0xFF0284C7)
                )
            }

            item {
                StepCard(
                    stepNumber = "4",
                    title = "Instant Reward & Cash Out",
                    description = "Receive +${appSettings.rewardPointsPerCycle} Point credited to your balance immediately upon verified ad completion. Cash out anytime to bKash, Nagad, or BEP20 USDT!",
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = GreenSuccess
                )
            }

            // Strict Ad Policy Note
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = RedLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = RedError)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Official Policy & Rules",
                                fontWeight = FontWeight.Bold,
                                color = RedError,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Never click on ads intentionally or use automated auto-clickers. Clicks are NOT rewarded.\n• Multiple accounts, emulator abuse, or fast quiz skipping will result in automated account suspension.\n• Rewards are granted strictly on legitimate video completion.",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Support & Channels
            item {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appSettings.telegramUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(Icons.Default.Send, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join Telegram Discussion Group", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StepCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = accentColor
                    ) {
                        Text(
                            text = "STEP $stepNumber",
                            color = SurfaceWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)
            }
        }
    }
}
