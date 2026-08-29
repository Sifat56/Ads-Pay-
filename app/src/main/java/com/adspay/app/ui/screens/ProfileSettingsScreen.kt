package com.adspay.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adspay.app.data.models.User
import com.adspay.app.data.models.UserRole
import com.adspay.app.data.repository.AdsPayRepository
import com.adspay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    user: User,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val appSettings by AdsPayRepository.appSettings.collectAsState()
    var showAboutDialog by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
    var serverUrlInput by remember { mutableStateOf(com.adspay.app.data.api.ApiConfig.getBaseUrl()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold, color = SurfaceWhite) },
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
            // User Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PurpleLighter,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = (user.name.firstOrNull() ?: 'U').uppercase(),
                                    color = PurpleDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )

                        Text(
                            text = user.email,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PurpleSubtle
                            ) {
                                Text(
                                    text = "ID: ${user.id}",
                                    color = PurplePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GoldLight
                            ) {
                                Text(
                                    text = "REF: ${user.referralCode}",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Account Overview & Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ACCOUNT SUMMARY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Earned", fontSize = 11.sp, color = TextSecondary)
                                Text("${user.totalEarned} pts", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PurplePrimary)
                            }
                            Column {
                                Text("Total Withdrawn", fontSize = 11.sp, color = TextSecondary)
                                Text("${user.totalWithdrawn} pts", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            }
                            Column {
                                Text("Completed Tasks", fontSize = 11.sp, color = TextSecondary)
                                Text("${user.completedQuizzesCount}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PurpleDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = DividerColor)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Account Status", fontSize = 13.sp, color = TextSecondary)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (user.isBlocked) RedLight else PurpleSubtle
                            ) {
                                Text(
                                    text = if (user.isBlocked) "Suspended" else "Active & Verified",
                                    color = if (user.isBlocked) RedError else PurplePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // General Information & Links
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "COMMUNITY & SUPPORT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        SettingRow(
                            title = "Telegram Channel",
                            subtitle = appSettings.telegramUrl,
                            icon = Icons.Default.Send,
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appSettings.telegramUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = DividerColor)

                        SettingRow(
                            title = "YouTube Channel",
                            subtitle = appSettings.youtubeUrl,
                            icon = Icons.Default.SmartDisplay,
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appSettings.youtubeUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = DividerColor)

                        SettingRow(
                            title = "Customer Support",
                            subtitle = appSettings.supportContact,
                            icon = Icons.Default.HeadsetMic,
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:${appSettings.supportContact}")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = DividerColor)

                        SettingRow(
                            title = "Server Connection / API",
                            subtitle = com.adspay.app.data.api.ApiConfig.getBaseUrl(),
                            icon = Icons.Default.CloudSync,
                            onClick = { 
                                serverUrlInput = com.adspay.app.data.api.ApiConfig.getBaseUrl()
                                showServerDialog = true 
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = DividerColor)

                        SettingRow(
                            title = "About Ads Pay",
                            subtitle = "Version 1.0 • Start.io SDK • Security Rules",
                            icon = Icons.Default.Info,
                            onClick = { showAboutDialog = true }
                        )
                    }
                }
            }

            // Logout Button
            item {
                Button(
                    onClick = {
                        AdsPayRepository.logout()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedError)
                ) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout from Account", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showServerDialog) {
            AlertDialog(
                onDismissRequest = { showServerDialog = false },
                title = { Text("Server & API Endpoint", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "Enter the Base URL of your Ads Pay backend server (e.g., https://your-server.com or local proxy).",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = serverUrlInput,
                            onValueChange = { serverUrlInput = it },
                            label = { Text("Server Base URL") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (serverUrlInput.isNotBlank()) {
                                com.adspay.app.data.api.ApiConfig.updateBaseUrl(serverUrlInput, context)
                                AdsPayRepository.syncRemoteSettings()
                                AdsPayRepository.syncUserProfile()
                            }
                            showServerDialog = false
                        }
                    ) {
                        Text("Save & Connect")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showServerDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("About Ads Pay", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(appSettings.aboutText, fontSize = 13.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("• Package: com.adspay.app", fontSize = 11.sp, color = TextMuted)
                        Text("• Start.io App ID: ${appSettings.startIoAppId}", fontSize = 11.sp, color = TextMuted)
                        Text("• Version: 1.0 (Production-Ready)", fontSize = 11.sp, color = TextMuted)
                    }
                },
                confirmButton = {
                    Button(onClick = { showAboutDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = PurpleSubtle,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}
