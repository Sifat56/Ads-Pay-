package com.adspay.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adspay.app.data.models.User
import com.adspay.app.data.models.UserRole
import com.adspay.app.data.repository.AdsPayRepository
import com.adspay.app.ui.components.*
import com.adspay.app.ui.theme.*

@Composable
fun HomeScreen(
    user: User,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val appSettings by AdsPayRepository.appSettings.collectAsState()
    val notifications by AdsPayRepository.notifications.collectAsState()
    val unreadNotifs = notifications.count { !it.isRead }

    val pointsValue = user.points * appSettings.pointMonetaryValue

    Scaffold(
        topBar = {
            AdsPayTopBar(
                user = user,
                unreadNotificationsCount = unreadNotifs,
                onNotificationsClick = { onNavigate("notifications") },
                onAdminClick = { onNavigate("admin") },
                onProfileClick = { onNavigate("profile") }
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Announcement Banner
            if (appSettings.announcementText.isNotBlank()) {
                item {
                    Surface(
                        color = GoldLight,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = appSettings.announcementText,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // Main Balance & Reward Cycle Tracker Card
            item {
                MainBalanceCard(
                    points = user.points,
                    balanceCurrency = pointsValue,
                    currencySymbol = appSettings.currencySymbol,
                    quizzesCompletedToday = user.completedQuizzesCount,
                    currentCycleProgress = user.currentCycleQuizzes,
                    requiredQuizzes = appSettings.rewardCycleQuizzesCount,
                    onStartTaskClick = { onNavigate("task") },
                    onWithdrawClick = { onNavigate("wallet") }
                )
            }

            // Banner Ad Container
            if (appSettings.isBannerAdsEnabled) {
                item {
                    StartIoBannerComposable(isEnabled = true)
                }
            }

            // Primary Quick Actions Grid
            item {
                Text(
                    text = "EARNING & ACTIONS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        GridMenuCard(
                            title = "Start Task",
                            icon = Icons.Default.PlayCircle,
                            iconBgColor = PurpleLighter,
                            iconTint = PurplePrimary,
                            badgeText = "${user.currentCycleQuizzes}/${appSettings.rewardCycleQuizzesCount}",
                            onClick = { onNavigate("task") }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        GridMenuCard(
                            title = "Cash Out",
                            icon = Icons.Default.AccountBalanceWallet,
                            iconBgColor = GreenLight,
                            iconTint = GreenSuccess,
                            badgeText = "Fast Pay",
                            onClick = { onNavigate("wallet") }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        GridMenuCard(
                            title = "Refer & Earn",
                            icon = Icons.Default.Share,
                            iconBgColor = GoldLight,
                            iconTint = GoldAccent,
                            badgeText = "10% Bonus",
                            onClick = { onNavigate("refer") }
                        )
                    }
                }
            }

            // Secondary Options Menu List
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "MENU & COMMUNITY",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 20.dp, bottom = 6.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionCard(
                        title = "Work History & Ledger",
                        subtitle = "Track completed quizzes, ads watched, and rewards",
                        icon = Icons.Default.History,
                        iconBgColor = PurpleSubtle,
                        iconTint = PurplePrimary,
                        onClick = { onNavigate("history") }
                    )

                    ActionCard(
                        title = "Top Chart / Leaderboard",
                        subtitle = "See highest earning members this month",
                        icon = Icons.Default.EmojiEvents,
                        iconBgColor = GoldLight,
                        iconTint = GoldAccent,
                        badgeText = "Top 50",
                        onClick = { onNavigate("leaderboard") }
                    )

                    ActionCard(
                        title = "How to Work & Rules",
                        subtitle = "Step-by-step tutorial on earning and payouts",
                        icon = Icons.Default.MenuBook,
                        iconBgColor = BlueLight,
                        iconTint = BlueInfo,
                        onClick = { onNavigate("how_to_work") }
                    )

                    ActionCard(
                        title = "Official Telegram Channel",
                        subtitle = "Join daily payment proofs & announcements group",
                        icon = Icons.Default.Send,
                        iconBgColor = Color(0xFFE0F2FE),
                        iconTint = Color(0xFF0284C7),
                        badgeText = "Join",
                        badgeColor = Color(0xFF0284C7),
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appSettings.telegramUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                onNavigate("how_to_work")
                            }
                        }
                    )

                    ActionCard(
                        title = "Official YouTube Channel",
                        subtitle = "Video guides and task walkthroughs",
                        icon = Icons.Default.SmartDisplay,
                        iconBgColor = RedLight,
                        iconTint = RedError,
                        badgeText = "Watch",
                        badgeColor = RedError,
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appSettings.youtubeUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                onNavigate("how_to_work")
                            }
                        }
                    )

                    if (user.role == UserRole.ADMIN) {
                        ActionCard(
                            title = "🛡️ Admin Control Panel",
                            subtitle = "Manage features, user bans, point adjustments, & withdrawals",
                            icon = Icons.Default.AdminPanelSettings,
                            iconBgColor = GoldLight,
                            iconTint = GoldAccent,
                            badgeText = "ADMIN",
                            badgeColor = GoldAccent,
                            onClick = { onNavigate("admin") }
                        )
                    }
                }
            }
        }
    }
}
