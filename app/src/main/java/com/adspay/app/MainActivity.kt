package com.adspay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adspay.app.data.models.UserRole
import com.adspay.app.data.repository.AdsPayRepository
import com.adspay.app.ui.components.AdsPayBottomBar
import com.adspay.app.ui.screens.*
import com.adspay.app.ui.theme.*

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
    val currentUser by AdsPayRepository.currentUser.collectAsState()
    val appSettings by AdsPayRepository.appSettings.collectAsState()

    var currentRoute by remember { mutableStateOf("home") }

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
