package com.adspay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adspay.app.data.NetworkUtils
import com.adspay.app.data.models.UserRole
import com.adspay.app.data.repository.AdsPayRepository
import com.adspay.app.ui.components.AdsPayBottomBar
import com.adspay.app.ui.screens.*
import com.adspay.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdsPayTheme {
                AdsPayMainApp()
            }
        }
    }
}

@Composable
fun AdsPayMainApp() {
    val context = LocalContext.current
    val isOnline by NetworkUtils.isOnlineState.collectAsState()
    val currentUser by AdsPayRepository.currentUser.collectAsState()
    val appSettings by AdsPayRepository.appSettings.collectAsState()

    var currentRoute by remember { mutableStateOf("home") }

    // STRICT 100% ONLINE-ONLY ENFORCEMENT:
    // If there is no internet connection, block ALL app screens, dashboard, and offline data access immediately!
    if (!isOnline) {
        OfflineBlockingScreen(
            onRetry = {
                NetworkUtils.refreshConnectivity(context)
            }
        )
        return
    }

    // If maintenance mode is active and user is not admin
    if (appSettings.isMaintenanceMode && currentUser?.role != UserRole.ADMIN) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = PurpleDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Under Scheduled Maintenance",
                    color = SurfaceWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = appSettings.maintenanceMessage,
                    color = PurpleLighter,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
        return
    }

    // If user is blocked
    if (currentUser?.isBlocked == true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = RedLight
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = RedError,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Account Suspended",
                    color = RedError,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your account has been restricted due to violation of anti-fraud or ad-clicking policies. Please contact support: ${appSettings.supportContact}",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { AdsPayRepository.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError)
                ) {
                    Text("Logout")
                }
            }
        }
        return
    }

    val user = currentUser
    if (user == null) {
        // Auth screen
        AuthScreen(
            onAuthSuccess = {
                currentRoute = "home"
            }
        )
    } else {
        val bottomNavRoutes = setOf("home", "task", "wallet", "refer", "leaderboard")
        val showBottomBar = currentRoute in bottomNavRoutes

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    AdsPayBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { currentRoute = it }
                    )
                }
            },
            containerColor = BackgroundLight
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
            ) {
                AnimatedContent(
                    targetState = currentRoute,
                    label = "screen_transition"
                ) { route ->
                    when (route) {
                        "home" -> HomeScreen(
                            user = user,
                            onNavigate = { currentRoute = it }
                        )
                        "task" -> StartTaskScreen(
                            user = user,
                            onNavigateBack = { currentRoute = "home" },
                            onNavigateToWallet = { currentRoute = "wallet" }
                        )
                        "wallet" -> WithdrawScreen(
                            user = user,
                            onNavigateBack = { currentRoute = "home" }
                        )
                        "refer" -> ReferralScreen(
                            user = user,
                            onNavigateBack = { currentRoute = "home" }
                        )
                        "leaderboard" -> LeaderboardScreen(
                            currentUser = user,
                            onNavigateBack = { currentRoute = "home" }
                        )
                        "history" -> WorkHistoryScreen(
                            user = user,
                            onNavigateBack = { currentRoute = "home" }
                        )
                        "how_to_work" -> HowToWorkScreen(
                            onNavigateBack = { currentRoute = "home" }
                        )
                        "notifications" -> NotificationsScreen(
                            user = user,
                            onNavigateBack = { currentRoute = "home" }
                        )
                        "profile" -> ProfileSettingsScreen(
                            user = user,
                            onNavigateBack = { currentRoute = "home" },
                            onLogout = { currentRoute = "home" }
                        )
                        else -> HomeScreen(
                            user = user,
                            onNavigate = { currentRoute = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineBlockingScreen(
    onRetry: () -> Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("No active internet connection found.") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PurpleDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Offline Emblem Icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "No Internet",
                    tint = GoldAccent,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Internet Connection Required",
                color = SurfaceWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ads Pay operates exclusively online. An active internet connection is required to authenticate, perform quiz tasks, watch rewarded ads, and sync your reward balance safely.",
                color = PurpleLighter,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Status chip
            Surface(
                color = SurfaceWhite.copy(alpha = 0.06f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(RedError)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusMessage,
                        color = SurfaceWhite.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (!isChecking) {
                        isChecking = true
                        statusMessage = "Testing connection..."
                        coroutineScope.launch {
                            delay(600)
                            val isConnected = onRetry()
                            isChecking = false
                            if (!isConnected) {
                                statusMessage = "Still offline. Please check Wi-Fi / Mobile Data."
                            }
                        }
                    }
                },
                enabled = !isChecking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = TextPrimary,
                    disabledContainerColor = GoldAccent.copy(alpha = 0.5f)
                )
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TextPrimary,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Checking Connection...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Retry Connection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

