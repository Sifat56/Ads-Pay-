package com.adspay.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adspay.app.data.models.RewardTransaction
import com.adspay.app.data.models.TransactionType
import com.adspay.app.data.models.User
import com.adspay.app.data.repository.AdsPayRepository
import com.adspay.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkHistoryScreen(
    user: User,
    onNavigateBack: () -> Unit
) {
    val allTransactions by AdsPayRepository.transactions.collectAsState()
    val userTx = remember(allTransactions, user.id) {
        allTransactions.filter { it.userId == user.id }
    }

    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterTabs = listOf("All", "Tasks & Ads", "Referrals", "Cash Outs")

    val filteredList = remember(userTx, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> userTx.filter { it.type == TransactionType.REWARD_CYCLE || it.type == TransactionType.SIGNUP_BONUS }
            2 -> userTx.filter { it.type == TransactionType.REFERRAL_BONUS }
            3 -> userTx.filter { it.type == TransactionType.WITHDRAWAL_DEDUCT || it.type == TransactionType.WITHDRAWAL_REFUND }
            else -> userTx
        }
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Work & Reward History", fontWeight = FontWeight.Bold, color = SurfaceWhite) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Scrollable Row
            ScrollableTabRow(
                selectedTabIndex = selectedFilterIndex,
                containerColor = SurfaceWhite,
                edgePadding = 16.dp,
                contentColor = PurplePrimary
            ) {
                filterTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedFilterIndex == index,
                        onClick = { selectedFilterIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HistoryToggleOff, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No history records found in this category.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList) { tx ->
                        val isPositive = tx.points >= 0

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isPositive) GreenLight else RedLight,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = when (tx.type) {
                                                    TransactionType.REWARD_CYCLE -> Icons.Default.PlayCircle
                                                    TransactionType.REFERRAL_BONUS -> Icons.Default.People
                                                    TransactionType.SIGNUP_BONUS -> Icons.Default.CardGiftcard
                                                    TransactionType.MANUAL_ADJUSTMENT -> Icons.Default.AdminPanelSettings
                                                    TransactionType.WITHDRAWAL_DEDUCT -> Icons.Default.AccountBalanceWallet
                                                    TransactionType.WITHDRAWAL_REFUND -> Icons.Default.Refresh
                                                },
                                                contentDescription = null,
                                                tint = if (isPositive) GreenSuccess else RedError,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = tx.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = tx.description,
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            maxLines = 2
                                        )
                                        Text(
                                            text = dateFormat.format(Date(tx.timestamp)),
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Text(
                                    text = "${if (isPositive) "+" else ""}${String.format("%.1f", tx.points)} pts",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = if (isPositive) GreenSuccess else RedError
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
