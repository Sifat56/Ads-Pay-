package com.adspay.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adspay.app.data.models.User
import com.adspay.app.data.repository.AdsPayRepository
import com.adspay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    currentUser: User,
    onNavigateBack: () -> Unit
) {
    val allUsers by AdsPayRepository.userList.collectAsState()
    val appSettings by AdsPayRepository.appSettings.collectAsState()

    // Sorted by total points earned
    val sortedLeaders = remember(allUsers) {
        allUsers.sortedByDescending { it.totalEarned }
    }

    // Mask privacy safely
    fun maskIdentifier(email: String, phone: String): String {
        return if (email.contains("@")) {
            val prefix = email.substringBefore("@")
            val maskedPrefix = if (prefix.length > 3) prefix.take(3) + "***" else prefix + "***"
            val domain = email.substringAfter("@")
            "$maskedPrefix@$domain"
        } else if (phone.length > 6) {
            phone.take(4) + "****" + phone.takeLast(3)
        } else {
            "Member"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Top Chart / Leaderboard", fontWeight = FontWeight.Bold, color = SurfaceWhite) },
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
        if (!appSettings.isLeaderboardEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Leaderboard is temporarily unavailable.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top 3 Podium Box
                if (sortedLeaders.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, RoundedCornerShape(20.dp)),
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
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Top Earners This Month",
                                        color = SurfaceWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "Earn points continuously to climb the leaderboard rankings",
                                        color = PurpleLighter.copy(alpha = 0.9f),
                                        fontSize = 12.sp
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Top 3 Horizontal Display
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        // 2nd Place
                                        sortedLeaders.getOrNull(1)?.let { u2 ->
                                            PodiumColumn(user = u2, rank = 2, medalColor = Color(0xFFC0C0C0))
                                        }
                                        // 1st Place
                                        sortedLeaders.getOrNull(0)?.let { u1 ->
                                            PodiumColumn(user = u1, rank = 1, medalColor = GoldAccent)
                                        }
                                        // 3rd Place
                                        sortedLeaders.getOrNull(2)?.let { u3 ->
                                            PodiumColumn(user = u3, rank = 3, medalColor = Color(0xFFCD7F32))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "ALL RANKINGS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                itemsIndexed(sortedLeaders) { index, leader ->
                    val isCurrent = leader.id == currentUser.id
                    val rank = index + 1

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) PurpleSubtle else SurfaceWhite
                        ),
                        border = if (isCurrent) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(PurplePrimary, PurpleLight))) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Rank Badge
                                Surface(
                                    shape = CircleShape,
                                    color = when (rank) {
                                        1 -> GoldAccent
                                        2 -> Color(0xFFC0C0C0)
                                        3 -> Color(0xFFCD7F32)
                                        else -> PurpleLighter
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$rank",
                                            color = if (rank <= 3) TextPrimary else PurpleDark,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = leader.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                        if (isCurrent) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = PurplePrimary
                                            ) {
                                                Text(
                                                    text = "YOU",
                                                    color = SurfaceWhite,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = maskIdentifier(leader.email, leader.phone),
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${String.format("%.1f", leader.totalEarned)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "${leader.completedQuizzesCount} Quizzes",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    user: User,
    rank: Int,
    medalColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = medalColor,
            modifier = Modifier.size(if (rank == 1) 48.dp else 40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "#$rank",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (rank == 1) 16.sp else 13.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user.name.take(8),
            color = SurfaceWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1
        )
        Text(
            text = "${user.totalEarned.toInt()} pts",
            color = GoldAccent,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}
