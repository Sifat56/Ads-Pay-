package com.adspay.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adspay.app.data.models.*
import com.adspay.app.data.repository.AdsPayRepository
import com.adspay.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPortalScreen(
    currentUser: User,
    onNavigateBack: () -> Unit
) {
    val appSettings by AdsPayRepository.appSettings.collectAsState()
    val allUsers by AdsPayRepository.userList.collectAsState()
    val allWithdrawals by AdsPayRepository.withdrawals.collectAsState()
    val allQuizzes by AdsPayRepository.quizzes.collectAsState()
    val auditLogs by AdsPayRepository.auditLogs.collectAsState()

    var selectedAdminTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Toggles & Limits", "Users", "Withdrawals", "Quizzes", "Broadcast", "Audit Logs")

    var userSearchQuery by remember { mutableStateOf("") }
    var selectedUserForEdit by remember { mutableStateOf<User?>(null) }
    var pointAdjustmentAmount by remember { mutableStateOf("") }
    var pointAdjustmentReason by remember { mutableStateOf("") }
    var showAdjustmentDialog by remember { mutableStateOf(false) }

    // Withdrawal action dialog
    var selectedWithdrawalForAction by remember { mutableStateOf<WithdrawalRequest?>(null) }
    var adminNoteInput by remember { mutableStateOf("") }
    var showWithdrawalDialog by remember { mutableStateOf(false) }

    // Add quiz dialog
    var showAddQuizDialog by remember { mutableStateOf(false) }
    var newQuizQuestion by remember { mutableStateOf("") }
    var newQuizOptA by remember { mutableStateOf("") }
    var newQuizOptB by remember { mutableStateOf("") }
    var newQuizOptC by remember { mutableStateOf("") }
    var newQuizOptD by remember { mutableStateOf("") }
    var newQuizCorrectIndex by remember { mutableIntStateOf(0) }
    var newQuizCategory by remember { mutableStateOf("General") }

    // Broadcast notification state
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastMessage by remember { mutableStateOf("") }
    var broadcastStatus by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🛡️ Admin Master Panel", fontWeight = FontWeight.Bold, color = SurfaceWhite, fontSize = 16.sp)
                        Text("Signed as ${currentUser.name}", fontSize = 11.sp, color = GoldAccent)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SurfaceWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PurpleDarker)
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Horizontal Admin Tab Bar
            ScrollableTabRow(
                selectedTabIndex = selectedAdminTab,
                containerColor = SurfaceWhite,
                edgePadding = 12.dp,
                contentColor = PurplePrimary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedAdminTab == index,
                        onClick = { selectedAdminTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            when (selectedAdminTab) {
                0 -> {
                    // --- TAB 0: OVERVIEW DASHBOARD ---
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("APP METRICS & FINANCIALS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AdminStatBox(
                                    title = "Total Users",
                                    value = "${allUsers.size}",
                                    icon = Icons.Default.People,
                                    bgColor = PurpleSubtle,
                                    textColor = PurplePrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminStatBox(
                                    title = "Total Points Issued",
                                    value = String.format("%.0f", allUsers.sumOf { it.totalEarned }),
                                    icon = Icons.Default.Stars,
                                    bgColor = GoldLight,
                                    textColor = GoldAccent,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val pendingWth = allWithdrawals.filter { it.status == WithdrawalStatus.PENDING }
                                val paidWth = allWithdrawals.filter { it.status == WithdrawalStatus.PAID }

                                AdminStatBox(
                                    title = "Pending Cashouts",
                                    value = "${pendingWth.size} (${pendingWth.sumOf { it.points }.toInt()} pts)",
                                    icon = Icons.Default.HourglassEmpty,
                                    bgColor = BlueLight,
                                    textColor = BlueInfo,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminStatBox(
                                    title = "Paid Cashouts",
                                    value = "${paidWth.size} (${paidWth.sumOf { it.amountCurrency }.toInt()} ৳)",
                                    icon = Icons.Default.CheckCircle,
                                    bgColor = GreenLight,
                                    textColor = GreenSuccess,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AdminStatBox(
                                    title = "Total Quizzes",
                                    value = "${allQuizzes.size} (${allQuizzes.count { it.isActive }} Active)",
                                    icon = Icons.Default.Quiz,
                                    bgColor = PurpleSubtle,
                                    textColor = PurpleDark,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminStatBox(
                                    title = "Blocked Users",
                                    value = "${allUsers.count { it.isBlocked }}",
                                    icon = Icons.Default.Block,
                                    bgColor = RedLight,
                                    textColor = RedError,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // --- TAB 1: TOGGLES & LIMITS ---
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("CORE FEATURE ON/OFF TOGGLES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    AdminToggleRow(
                                        title = "Maintenance Mode",
                                        subtitle = "Show maintenance screen to all regular users",
                                        isChecked = appSettings.isMaintenanceMode,
                                        onCheckedChange = { AdsPayRepository.updateAppSettings(appSettings.copy(isMaintenanceMode = it)) }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerColor)
                                    AdminToggleRow(
                                        title = "User Registration",
                                        subtitle = "Allow new users to sign up",
                                        isChecked = appSettings.isRegistrationEnabled,
                                        onCheckedChange = { AdsPayRepository.updateAppSettings(appSettings.copy(isRegistrationEnabled = it)) }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerColor)
                                    AdminToggleRow(
                                        title = "Start Task System",
                                        subtitle = "Enable quizzes and daily reward tasks",
                                        isChecked = appSettings.isTaskSystemEnabled,
                                        onCheckedChange = { AdsPayRepository.updateAppSettings(appSettings.copy(isTaskSystemEnabled = it)) }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerColor)
                                    AdminToggleRow(
                                        title = "Start.io Rewarded Ads",
                                        subtitle = "Serve official Start.io video ads after cycle",
                                        isChecked = appSettings.isRewardedAdsEnabled,
                                        onCheckedChange = { AdsPayRepository.updateAppSettings(appSettings.copy(isRewardedAdsEnabled = it)) }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerColor)
                                    AdminToggleRow(
                                        title = "Start.io Banner Ads",
                                        subtitle = "Display banner ads on quiz screens",
                                        isChecked = appSettings.isBannerAdsEnabled,
                                        onCheckedChange = { AdsPayRepository.updateAppSettings(appSettings.copy(isBannerAdsEnabled = it)) }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerColor)
                                    AdminToggleRow(
                                        title = "Referral Program",
                                        subtitle = "Enable 10% referral commissions",
                                        isChecked = appSettings.isReferralEnabled,
                                        onCheckedChange = { AdsPayRepository.updateAppSettings(appSettings.copy(isReferralEnabled = it)) }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerColor)
                                    AdminToggleRow(
                                        title = "Withdraw / Cash Out System",
                                        subtitle = "Allow users to request point payouts",
                                        isChecked = appSettings.isWithdrawEnabled,
                                        onCheckedChange = { AdsPayRepository.updateAppSettings(appSettings.copy(isWithdrawEnabled = it)) }
                                    )
                                }
                            }
                        }

                        item {
                            Text("PAYOUT GATEWAYS ENABLED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    AdminToggleRow(
                                        title = "bKash Payouts",
                                        subtitle = "Mobile wallet payout in Bangladesh",
                                        isChecked = appSettings.isBkashEnabled,
                                        onCheckedChange = { AdsPayRepository.updateAppSettings(appSettings.copy(isBkashEnabled = it)) }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerColor)
                                    AdminToggleRow(
                                        title = "Nagad Payouts",
                                        subtitle = "Mobile wallet payout in Bangladesh",
                                        isChecked = appSettings.isNagadEnabled,
                                        onCheckedChange = { AdsPayRepository.updateAppSettings(appSettings.copy(isNagadEnabled = it)) }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerColor)
                                    AdminToggleRow(
                                        title = "USDT (BEP20 Network)",
                                        subtitle = "Crypto wallet payout",
                                        isChecked = appSettings.isUsdtEnabled,
                                        onCheckedChange = { AdsPayRepository.updateAppSettings(appSettings.copy(isUsdtEnabled = it)) }
                                    )
                                }
                            }
                        }

                        item {
                            Text("REWARD SETTINGS & FINANCIAL LIMITS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Quizzes Required per Rewarded Ad Cycle: ${appSettings.rewardCycleQuizzesCount}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(3, 5, 8, 10).forEach { count ->
                                            OutlinedButton(
                                                onClick = { AdsPayRepository.updateAppSettings(appSettings.copy(rewardCycleQuizzesCount = count)) },
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (appSettings.rewardCycleQuizzesCount == count) PurplePrimary else Color.Transparent
                                                )
                                            ) {
                                                Text(
                                                    "$count Quizzes",
                                                    color = if (appSettings.rewardCycleQuizzesCount == count) SurfaceWhite else TextPrimary
                                                )
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = DividerColor)

                                    Text("Reward Points per Video Ad Completion: ${appSettings.rewardPointsPerCycle} pt", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(0.5, 1.0, 2.0, 5.0).forEach { pt ->
                                            OutlinedButton(
                                                onClick = { AdsPayRepository.updateAppSettings(appSettings.copy(rewardPointsPerCycle = pt)) },
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (appSettings.rewardPointsPerCycle == pt) PurplePrimary else Color.Transparent
                                                )
                                            ) {
                                                Text(
                                                    "$pt pt",
                                                    color = if (appSettings.rewardPointsPerCycle == pt) SurfaceWhite else TextPrimary
                                                )
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = DividerColor)

                                    Text("Monetary Value per 1 Point: ${appSettings.currencySymbol} ${appSettings.pointMonetaryValue}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(0.10, 0.20, 0.50, 1.00).forEach { valPt ->
                                            OutlinedButton(
                                                onClick = { AdsPayRepository.updateAppSettings(appSettings.copy(pointMonetaryValue = valPt)) },
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (appSettings.pointMonetaryValue == valPt) PurplePrimary else Color.Transparent
                                                )
                                            ) {
                                                Text(
                                                    "৳ $valPt",
                                                    color = if (appSettings.pointMonetaryValue == valPt) SurfaceWhite else TextPrimary
                                                )
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = DividerColor)

                                    Text("Minimum Withdrawal Points: ${appSettings.minWithdrawalPoints.toInt()} pts", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(25.0, 50.0, 100.0, 200.0).forEach { minPt ->
                                            OutlinedButton(
                                                onClick = { AdsPayRepository.updateAppSettings(appSettings.copy(minWithdrawalPoints = minPt)) },
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (appSettings.minWithdrawalPoints == minPt) PurplePrimary else Color.Transparent
                                                )
                                            ) {
                                                Text(
                                                    "${minPt.toInt()} pts",
                                                    color = if (appSettings.minWithdrawalPoints == minPt) SurfaceWhite else TextPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // --- TAB 2: USER MANAGEMENT ---
                    val filteredUsers = remember(allUsers, userSearchQuery) {
                        if (userSearchQuery.isBlank()) allUsers
                        else allUsers.filter {
                            it.name.contains(userSearchQuery, ignoreCase = true) ||
                            it.email.contains(userSearchQuery, ignoreCase = true) ||
                            it.id.contains(userSearchQuery, ignoreCase = true) ||
                            it.referralCode.contains(userSearchQuery, ignoreCase = true)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = userSearchQuery,
                                onValueChange = { userSearchQuery = it },
                                label = { Text("Search by Name, Email, ID, or Ref Code") },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        items(filteredUsers) { u ->
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
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(u.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                if (u.isBlocked) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(shape = RoundedCornerShape(4.dp), color = RedLight) {
                                                        Text("BLOCKED", color = RedError, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                                    }
                                                }
                                            }
                                            Text("${u.email} • ID: ${u.id}", fontSize = 11.sp, color = TextSecondary)
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                "${String.format("%.1f", u.points)} pts",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = PurplePrimary
                                            )
                                            Text("Ref: ${u.referralCode}", fontSize = 11.sp, color = TextMuted)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                selectedUserForEdit = u
                                                pointAdjustmentAmount = "10"
                                                pointAdjustmentReason = "Loyalty bonus"
                                                showAdjustmentDialog = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Adjust Pts", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                AdsPayRepository.toggleUserRestriction(u.id, block = !u.isBlocked)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (u.isBlocked) GreenSuccess else RedError
                                            ),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(if (u.isBlocked) "Unblock" else "Block", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // --- TAB 3: WITHDRAWAL MANAGEMENT ---
                    var selectedWthStatusFilter by remember { mutableStateOf<WithdrawalStatus?>(null) }

                    val filteredWth = remember(allWithdrawals, selectedWthStatusFilter) {
                        if (selectedWthStatusFilter == null) allWithdrawals
                        else allWithdrawals.filter { it.status == selectedWthStatusFilter }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            ScrollableTabRow(
                                selectedTabIndex = if (selectedWthStatusFilter == null) 0 else selectedWthStatusFilter!!.ordinal + 1,
                                containerColor = SurfaceWhite,
                                edgePadding = 8.dp
                            ) {
                                Tab(
                                    selected = selectedWthStatusFilter == null,
                                    onClick = { selectedWthStatusFilter = null },
                                    text = { Text("All (${allWithdrawals.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                                WithdrawalStatus.values().forEach { st ->
                                    Tab(
                                        selected = selectedWthStatusFilter == st,
                                        onClick = { selectedWthStatusFilter = st },
                                        text = { Text(st.name, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                    )
                                }
                            }
                        }

                        items(filteredWth) { w ->
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
                                        Column {
                                            Text(w.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Method: ${w.method.name} • ${w.accountInfo}", fontSize = 12.sp, color = TextSecondary)
                                        }
                                        StatusBadge(status = w.status)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Pay: ${w.amountCurrency} ${w.currencySymbol} (${w.points.toInt()} pts)",
                                            fontWeight = FontWeight.Bold,
                                            color = GreenSuccess
                                        )
                                        Text(dateFormat.format(Date(w.requestDate)), fontSize = 11.sp, color = TextMuted)
                                    }

                                    if (w.adminNote.isNotBlank()) {
                                        Text("Note: ${w.adminNote}", fontSize = 11.sp, color = PurpleDark)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (w.status == WithdrawalStatus.PENDING || w.status == WithdrawalStatus.PROCESSING) {
                                            Button(
                                                onClick = {
                                                    selectedWithdrawalForAction = w
                                                    adminNoteInput = "Paid via ${w.method.name} TxID: "
                                                    showWithdrawalDialog = true
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Mark Paid", fontSize = 11.sp)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    AdsPayRepository.updateWithdrawalStatus(
                                                        w.id,
                                                        WithdrawalStatus.REJECTED,
                                                        adminNote = "Rejected by admin. Points refunded."
                                                    )
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Reject & Refund", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // --- TAB 4: QUIZ / TASK MANAGEMENT ---
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Button(
                                onClick = { showAddQuizDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add New Quiz Question")
                            }
                        }

                        items(allQuizzes) { q ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = PurpleSubtle
                                        ) {
                                            Text(
                                                "${q.category} • ${q.timerSeconds}s",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PurplePrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    AdsPayRepository.updateQuiz(q.copy(isActive = !q.isActive))
                                                }
                                            ) {
                                                Icon(
                                                    if (q.isActive) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = null,
                                                    tint = if (q.isActive) GreenSuccess else TextMuted
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    AdsPayRepository.deleteQuiz(q.id)
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = RedError)
                                            }
                                        }
                                    }

                                    Text(q.question, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    q.options.forEachIndexed { i, opt ->
                                        Text(
                                            text = "${('A' + i)}. $opt ${if (i == q.correctOptionIndex) "✅ (Correct)" else ""}",
                                            fontSize = 11.sp,
                                            color = if (i == q.correctOptionIndex) GreenSuccess else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                5 -> {
                    // --- TAB 5: BROADCAST NOTIFICATIONS ---
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("PUSH ANNOUNCEMENT TO ALL USERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = broadcastTitle,
                                        onValueChange = { broadcastTitle = it },
                                        label = { Text("Notification Title") },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = broadcastMessage,
                                        onValueChange = { broadcastMessage = it },
                                        label = { Text("Message Body") },
                                        shape = RoundedCornerShape(10.dp),
                                        minLines = 3,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {
                                            if (broadcastTitle.isNotBlank() && broadcastMessage.isNotBlank()) {
                                                AdsPayRepository.addNotification(broadcastTitle, broadcastMessage, "ANNOUNCEMENT", "ALL")
                                                broadcastStatus = "Announcement broadcasted successfully to all users!"
                                                broadcastTitle = ""
                                                broadcastMessage = ""
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                                    ) {
                                        Icon(Icons.Default.Send, null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Send Push Broadcast")
                                    }

                                    broadcastStatus?.let {
                                        Text(it, color = GreenSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                6 -> {
                    // --- TAB 6: AUDIT LOGS ---
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text("ADMIN ACTION AUDIT TRAIL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        }

                        if (auditLogs.isEmpty()) {
                            item {
                                Text("No audit logs recorded yet.", color = TextSecondary, fontSize = 13.sp)
                            }
                        } else {
                            items(auditLogs) { log ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PurplePrimary)
                                            Text(dateFormat.format(Date(log.timestamp)), fontSize = 10.sp, color = TextMuted)
                                        }
                                        Text("Target: ${log.targetType} [${log.targetId}]", fontSize = 11.sp, color = TextPrimary)
                                        if (log.reason.isNotBlank()) {
                                            Text("Reason: ${log.reason}", fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Point Adjustment Dialog
        if (showAdjustmentDialog && selectedUserForEdit != null) {
            AlertDialog(
                onDismissRequest = { showAdjustmentDialog = false },
                title = { Text("Manual Balance Adjustment") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Target: ${selectedUserForEdit?.name} (${selectedUserForEdit?.points} pts)")
                        OutlinedTextField(
                            value = pointAdjustmentAmount,
                            onValueChange = { pointAdjustmentAmount = it },
                            label = { Text("Points (+ to credit, - to deduct)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = pointAdjustmentReason,
                            onValueChange = { pointAdjustmentReason = it },
                            label = { Text("Mandatory Reason") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val amt = pointAdjustmentAmount.toDoubleOrNull() ?: 0.0
                        if (amt != 0.0 && pointAdjustmentReason.isNotBlank()) {
                            AdsPayRepository.adjustUserPoints(selectedUserForEdit!!.id, amt, pointAdjustmentReason)
                            showAdjustmentDialog = false
                        }
                    }) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showAdjustmentDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Withdrawal Mark Paid Dialog
        if (showWithdrawalDialog && selectedWithdrawalForAction != null) {
            AlertDialog(
                onDismissRequest = { showWithdrawalDialog = false },
                title = { Text("Complete Cash Out") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("User: ${selectedWithdrawalForAction?.userName}")
                        Text("Pay: ${selectedWithdrawalForAction?.amountCurrency} ${selectedWithdrawalForAction?.currencySymbol} to ${selectedWithdrawalForAction?.accountInfo}")
                        OutlinedTextField(
                            value = adminNoteInput,
                            onValueChange = { adminNoteInput = it },
                            label = { Text("Transaction ID / Note for User") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        AdsPayRepository.updateWithdrawalStatus(
                            selectedWithdrawalForAction!!.id,
                            WithdrawalStatus.PAID,
                            adminNote = adminNoteInput
                        )
                        showWithdrawalDialog = false
                    }) {
                        Text("Confirm Paid")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showWithdrawalDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add Quiz Dialog
        if (showAddQuizDialog) {
            AlertDialog(
                onDismissRequest = { showAddQuizDialog = false },
                title = { Text("Add Quiz Question") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = newQuizQuestion, onValueChange = { newQuizQuestion = it }, label = { Text("Question") })
                        OutlinedTextField(value = newQuizOptA, onValueChange = { newQuizOptA = it }, label = { Text("Option A") })
                        OutlinedTextField(value = newQuizOptB, onValueChange = { newQuizOptB = it }, label = { Text("Option B") })
                        OutlinedTextField(value = newQuizOptC, onValueChange = { newQuizOptC = it }, label = { Text("Option C") })
                        OutlinedTextField(value = newQuizOptD, onValueChange = { newQuizOptD = it }, label = { Text("Option D") })
                        OutlinedTextField(value = newQuizCategory, onValueChange = { newQuizCategory = it }, label = { Text("Category") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (newQuizQuestion.isNotBlank() && newQuizOptA.isNotBlank() && newQuizOptB.isNotBlank()) {
                            val q = Quiz(
                                question = newQuizQuestion,
                                options = listOf(newQuizOptA, newQuizOptB, newQuizOptC.ifBlank { "None" }, newQuizOptD.ifBlank { "All" }),
                                correctOptionIndex = newQuizCorrectIndex,
                                category = newQuizCategory,
                                timerSeconds = 10,
                                isActive = true
                            )
                            AdsPayRepository.addQuiz(q)
                            showAddQuizDialog = false
                        }
                    }) {
                        Text("Save Quiz")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showAddQuizDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun AdminStatBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(
                shape = CircleShape,
                color = bgColor,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = textColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
        }
    }
}

@Composable
private fun AdminToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextSecondary)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SurfaceWhite,
                checkedTrackColor = PurplePrimary
            )
        )
    }
}
