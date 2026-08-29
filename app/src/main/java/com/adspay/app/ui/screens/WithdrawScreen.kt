package com.adspay.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adspay.app.data.models.User
import com.adspay.app.data.models.WithdrawMethod
import com.adspay.app.data.models.WithdrawalRequest
import com.adspay.app.data.models.WithdrawalStatus
import com.adspay.app.data.repository.AdsPayRepository
import com.adspay.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    user: User,
    onNavigateBack: () -> Unit
) {
    val appSettings by AdsPayRepository.appSettings.collectAsState()
    val allWithdrawals by AdsPayRepository.withdrawals.collectAsState()
    val userWithdrawals = remember(allWithdrawals, user.id) {
        allWithdrawals.filter { it.userId == user.id }
    }

    var selectedMethod by remember { mutableStateOf(WithdrawMethod.BKASH) }
    var pointsInput by remember { mutableStateOf(appSettings.minWithdrawalPoints.toInt().toString()) }
    var accountInfo by remember { mutableStateOf("") }
    var accountHolderName by remember { mutableStateOf(user.name) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Cashout Form, 1 = History

    val enteredPoints = pointsInput.toDoubleOrNull() ?: 0.0
    val calculatedCurrency = enteredPoints * appSettings.pointMonetaryValue

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Cash Out / Withdraw", fontWeight = FontWeight.Bold, color = SurfaceWhite)
                },
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
            // Tabs: Request Withdrawal vs My History
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceWhite,
                contentColor = PurplePrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Request Cash Out", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Withdrawal History (${userWithdrawals.size})", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Balance Header
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PurpleSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Available Balance", fontSize = 12.sp, color = TextSecondary)
                                    Text(
                                        "${String.format("%.1f", user.points)} Points",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PurpleDark
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Monetary Value", fontSize = 12.sp, color = TextSecondary)
                                    Text(
                                        "${appSettings.currencySymbol} ${String.format("%.2f", user.points * appSettings.pointMonetaryValue)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenSuccess
                                    )
                                }
                            }
                        }
                    }

                    // Alert Messages
                    item {
                        errorMessage?.let {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = RedLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(it, color = RedError, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                            }
                        }
                        successMessage?.let {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GreenLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(it, color = GreenSuccess, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                            }
                        }
                    }

                    // Select Payment Method
                    item {
                        Text("Select Payout Method", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (appSettings.isBkashEnabled) {
                                PaymentMethodChip(
                                    title = "bKash",
                                    subtitle = "Personal / Agent",
                                    isSelected = selectedMethod == WithdrawMethod.BKASH,
                                    onClick = { selectedMethod = WithdrawMethod.BKASH },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (appSettings.isNagadEnabled) {
                                PaymentMethodChip(
                                    title = "Nagad",
                                    subtitle = "Personal / Agent",
                                    isSelected = selectedMethod == WithdrawMethod.NAGAD,
                                    onClick = { selectedMethod = WithdrawMethod.NAGAD },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (appSettings.isUsdtEnabled) {
                                PaymentMethodChip(
                                    title = "USDT",
                                    subtitle = "BEP20 Network",
                                    isSelected = selectedMethod == WithdrawMethod.USDT_BEP20,
                                    onClick = { selectedMethod = WithdrawMethod.USDT_BEP20 },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Points Input
                    item {
                        OutlinedTextField(
                            value = pointsInput,
                            onValueChange = { pointsInput = it },
                            label = { Text("Points to Redeem (Min: ${appSettings.minWithdrawalPoints.toInt()} pts)") },
                            leadingIcon = { Icon(Icons.Default.Stars, null, tint = GoldAccent) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "You will receive: ${appSettings.currencySymbol} ${String.format("%.2f", calculatedCurrency)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenSuccess,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    // Account Number / Wallet address
                    item {
                        OutlinedTextField(
                            value = accountInfo,
                            onValueChange = { accountInfo = it },
                            label = {
                                Text(
                                    when (selectedMethod) {
                                        WithdrawMethod.BKASH -> "bKash Mobile Number (e.g. 01812345678)"
                                        WithdrawMethod.NAGAD -> "Nagad Mobile Number (e.g. 01912345678)"
                                        WithdrawMethod.USDT_BEP20 -> "BEP20 USDT Wallet Address (0x...)"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    if (selectedMethod == WithdrawMethod.USDT_BEP20) Icons.Default.AccountBalanceWallet else Icons.Default.PhoneAndroid,
                                    null,
                                    tint = PurplePrimary
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Account Holder Name
                    item {
                        OutlinedTextField(
                            value = accountHolderName,
                            onValueChange = { accountHolderName = it },
                            label = { Text("Account Holder / Receiver Name") },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = PurplePrimary) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Submit Cash Out Button
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                errorMessage = null
                                successMessage = null
                                if (enteredPoints < appSettings.minWithdrawalPoints) {
                                    errorMessage = "Minimum cash out is ${appSettings.minWithdrawalPoints} points."
                                    return@Button
                                }
                                if (enteredPoints > user.points) {
                                    errorMessage = "You do not have enough points. Balance: ${user.points} pts."
                                    return@Button
                                }
                                if (accountInfo.trim().length < 6) {
                                    errorMessage = "Please enter a valid phone number or BEP20 address."
                                    return@Button
                                }

                                coroutineScope.launch {
                                    isSubmitting = true
                                    val res = AdsPayRepository.requestWithdrawal(
                                        method = selectedMethod,
                                        points = enteredPoints,
                                        accountInfo = accountInfo,
                                        accountHolderName = accountHolderName
                                    )
                                    isSubmitting = false
                                    res.onSuccess {
                                        successMessage = "Cash out request of ${it.points} points submitted successfully! It will be reviewed by admin."
                                        accountInfo = ""
                                        selectedTab = 1
                                    }.onFailure {
                                        errorMessage = it.message
                                    }
                                }
                            },
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    color = SurfaceWhite,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Icon(Icons.Default.Payment, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Confirm & Request Cash Out", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            } else {
                // Withdrawal History List
                if (userWithdrawals.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No withdrawal requests yet.", color = TextMuted, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(userWithdrawals) { item ->
                            WithdrawalHistoryCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, if (isSelected) PurplePrimary else BorderLight, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (isSelected) PurpleSubtle else SurfaceWhite,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSelected) PurplePrimary else TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun WithdrawalHistoryCard(request: WithdrawalRequest) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = request.method.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }

                StatusBadge(status = request.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Account: ${request.accountInfo}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "${request.amountCurrency} ${request.currencySymbol} (${request.points.toInt()} pts)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
            }

            if (request.adminNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PurpleSubtle,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Note: ${request.adminNote}",
                        fontSize = 11.sp,
                        color = PurpleDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = dateFormat.format(Date(request.requestDate)),
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun StatusBadge(status: WithdrawalStatus) {
    val (bgColor, textColor, label) = when (status) {
        WithdrawalStatus.PENDING -> Triple(GoldLight, GoldAccent, "Pending")
        WithdrawalStatus.PROCESSING -> Triple(BlueLight, BlueInfo, "Processing")
        WithdrawalStatus.APPROVED -> Triple(PurpleLighter, PurplePrimary, "Approved")
        WithdrawalStatus.PAID -> Triple(GreenLight, GreenSuccess, "Paid")
        WithdrawalStatus.REJECTED -> Triple(RedLight, RedError, "Rejected")
        WithdrawalStatus.CANCELLED -> Triple(BorderLight, TextSecondary, "Cancelled")
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
