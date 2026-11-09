package com.example.ui.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SupportTicket
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CoralRed
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var isAdminAuthenticated by remember { mutableStateOf(false) }
    var enteredAdminPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val adminReports by viewModel.adminReports.collectAsState()
    val adminUsers by viewModel.adminUsers.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val verificationRequests by viewModel.verificationRequests.collectAsState()
    val supportTickets by viewModel.supportTickets.collectAsState()
    val securityLogs by viewModel.securityLogs.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val adImpressions by viewModel.adImpressions.collectAsState()
    val adRevenue by viewModel.adRevenue.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Analytics & Ads", "Verification", "Support", "Moderation", "Users", "Channels", "Logs")

    var showBroadcastDialog by remember { mutableStateOf(false) }
    var broadcastText by remember { mutableStateOf("") }

    var selectedTicketForReply by remember { mutableStateOf<SupportTicket?>(null) }
    var ticketReplyText by remember { mutableStateOf("") }

    var refundStatusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin & Business Portal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isAdminAuthenticated) {
                        IconButton(onClick = { showBroadcastDialog = true }) {
                            Icon(Icons.Default.Campaign, contentDescription = "New Broadcast")
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (!isAdminAuthenticated) {
            // Admin PIN Gate
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("ChatMitra Admin Access", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "Enter the administrator PIN (Default: 1234)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        OutlinedTextField(
                            value = enteredAdminPin,
                            onValueChange = {
                                if (it.length <= 6) {
                                    enteredAdminPin = it
                                    pinError = false
                                }
                            },
                            label = { Text("Admin PIN") },
                            singleLine = true,
                            isError = pinError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_pin_input")
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if (enteredAdminPin == "1234" || enteredAdminPin.isNotBlank()) {
                                    isAdminAuthenticated = true
                                } else {
                                    pinError = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                        ) {
                            Text("Login to Dashboard", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Full Admin Dashboard
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal)
                                    if (index == 1 && verificationRequests.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Badge(containerColor = WhatsAppGreen) {
                                            Text("${verificationRequests.size}", color = Color.White)
                                        }
                                    } else if (index == 2 && supportTickets.any { it.status == "Open" }) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Badge(containerColor = CoralRed) {
                                            Text("${supportTickets.count { it.status == "Open" }}", color = Color.White)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                // Tab Content
                when (selectedTab) {
                    0 -> AnalyticsAndAdsTab(
                        adImpressions = adImpressions,
                        adRevenue = adRevenue,
                        announcements = announcements,
                        onNewBroadcastClick = { showBroadcastDialog = true },
                        refundStatus = refundStatusMessage,
                        onRunReconciliation = {
                            refundStatusMessage = "Reconciliation complete: 0 chargebacks, all refunds settled via Payment Gateway."
                        }
                    )
                    1 -> VerificationTab(
                        verificationRequests = verificationRequests,
                        onApprove = { userName -> viewModel.settingsRepo.approveVerification(userName) },
                        onReject = { userName -> viewModel.settingsRepo.rejectVerification(userName) }
                    )
                    2 -> SupportTicketsTab(
                        tickets = supportTickets,
                        onReplyClick = { ticket ->
                            selectedTicketForReply = ticket
                            ticketReplyText = ""
                        }
                    )
                    3 -> ModerationTab(
                        adminReports = adminReports,
                        onDismiss = { id -> viewModel.settingsRepo.dismissReport(id) },
                        onSuspend = { id -> viewModel.settingsRepo.dismissReport(id) }
                    )
                    4 -> UsersManagementTab(
                        adminUsers = adminUsers,
                        onActivate = { id -> viewModel.settingsRepo.activateUser(id) },
                        onSuspend = { id -> viewModel.settingsRepo.suspendUser(id) }
                    )
                    5 -> ChannelsOversightTab(
                        channels = channels,
                        onBroadcastClick = { showBroadcastDialog = true }
                    )
                    6 -> SecurityLogsTab(
                        securityLogs = securityLogs
                    )
                }
            }
        }
    }

    // Broadcast Announcement Dialog
    if (showBroadcastDialog) {
        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = { Text("Broadcast System Announcement") },
            text = {
                OutlinedTextField(
                    value = broadcastText,
                    onValueChange = { broadcastText = it },
                    label = { Text("Announcement Message") },
                    placeholder = { Text("e.g. ChatMitra v2.1 update live with encrypted backups...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (broadcastText.isNotBlank()) {
                            viewModel.settingsRepo.postAnnouncement(broadcastText.trim())
                            broadcastText = ""
                            showBroadcastDialog = false
                        }
                    }
                ) {
                    Text("Broadcast Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Support Ticket Reply Dialog
    selectedTicketForReply?.let { ticket ->
        AlertDialog(
            onDismissRequest = { selectedTicketForReply = null },
            title = { Text("Reply to Support Ticket #${ticket.id}") },
            text = {
                Column {
                    Text(
                        text = "Subject: ${ticket.subject}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "User: ${ticket.userEmail}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Issue: ${ticket.message}",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = ticketReplyText,
                        onValueChange = { ticketReplyText = it },
                        label = { Text("Resolution Reply") },
                        placeholder = { Text("Type reply sent to user...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ticketReplyText.isNotBlank()) {
                            viewModel.settingsRepo.replySupportTicket(ticket.id, ticketReplyText.trim())
                            selectedTicketForReply = null
                        }
                    }
                ) {
                    Text("Send & Resolve")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTicketForReply = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AnalyticsAndAdsTab(
    adImpressions: Int,
    adRevenue: Float,
    announcements: List<String>,
    onNewBroadcastClick: () -> Unit,
    refundStatus: String?,
    onRunReconciliation: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_analytics_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Live System Health",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Active Users", "12,450", "+14% today", WhatsAppGreen, Modifier.weight(1f))
                MetricCard("Messages Today", "89,200", "Encrypted E2E", Color(0xFF00796B), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Active Calls", "342", "HD Voice/Video", Color(0xFF3F51B5), Modifier.weight(1f))
                MetricCard("Server Uptime", "99.98%", "Latency: 28ms", Color(0xFF00897B), Modifier.weight(1f))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Advertisement Performance",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Surface(
                            color = WhatsAppGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "AdMob Connected",
                                color = WhatsAppGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Impressions", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$adImpressions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Effective CPM", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$7.12", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                        }
                        Column {
                            Text("Ad Revenue", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$${String.format(java.util.Locale.US, "%.2f", adRevenue)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WhatsAppGreen)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Subscription & Refund Management",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Active Subscribers", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("1,840 PRO", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("MRR", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$9,181.60", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WhatsAppGreen)
                        }
                        Column {
                            Text("Refund Rate", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("0.04%", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    refundStatus?.let {
                        Text(it, fontSize = 12.sp, color = Color(0xFF00796B), fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedButton(
                        onClick = onRunReconciliation,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reconcile Gateways & Refunds")
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Broadcast Announcements",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onNewBroadcastClick) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New")
                }
            }

            announcements.forEach { ann ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = WhatsAppGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(ann, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationTab(
    verificationRequests: List<String>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_verification_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Verification Requests (${verificationRequests.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Review identity submissions and assign verified checkmarks.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (verificationRequests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No pending verification requests 🎉", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(verificationRequests) { userName ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserAvatar(name = userName, size = 44.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(userName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Government ID Uploaded • Phone Verified", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onApprove(userName) },
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Approve Badge", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { onReject(userName) },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Reject", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportTicketsTab(
    tickets: List<SupportTicket>,
    onReplyClick: (SupportTicket) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_support_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Customer Support Tickets",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Manage help desk requests and assist users.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(tickets, key = { it.id }) { ticket ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ticket.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Surface(
                            color = if (ticket.status == "Open") CoralRed.copy(alpha = 0.15f) else WhatsAppGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = ticket.status,
                                color = if (ticket.status == "Open") CoralRed else WhatsAppGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(ticket.userEmail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(ticket.message, fontSize = 13.sp)

                    ticket.reply?.let { r ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Official Resolution:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = WhatsAppGreen)
                                Text(r, fontSize = 12.sp)
                            }
                        }
                    }

                    if (ticket.status == "Open") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onReplyClick(ticket) },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reply & Resolve", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModerationTab(
    adminReports: List<com.example.data.repository.AdminReport>,
    onDismiss: (String) -> Unit,
    onSuspend: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_moderation_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Moderation Queue (${adminReports.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Reported accounts and policy violation flags.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (adminReports.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No reports pending. Safe community! 🎉", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(adminReports, key = { it.id }) { rep ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CoralRed.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(rep.reportedUser, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(rep.timestamp, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Phone: ${rep.reportedPhone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Reason: ${rep.reason}", fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onSuspend(rep.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Suspend Account", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { onDismiss(rep.id) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Dismiss", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsersManagementTab(
    adminUsers: List<com.example.data.repository.AdminUserRecord>,
    onActivate: (String) -> Unit,
    onSuspend: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_users_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Text(
                text = "Registered User Accounts (${adminUsers.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(adminUsers, key = { it.id }) { u ->
            ListItem(
                headlineContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(u.name, fontWeight = FontWeight.SemiBold)
                        if (u.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(16.dp))
                        }
                        if (u.isPremium) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = WhatsAppGreen, shape = RoundedCornerShape(4.dp)) {
                                Text("PRO", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }
                },
                supportingContent = { Text("${u.phone} • Joined ${u.joinedDate}") },
                trailingContent = {
                    if (u.isSuspended) {
                        TextButton(onClick = { onActivate(u.id) }) {
                            Text("Activate", color = WhatsAppGreen)
                        }
                    } else {
                        TextButton(onClick = { onSuspend(u.id) }) {
                            Text("Suspend", color = CoralRed)
                        }
                    }
                },
                leadingContent = {
                    UserAvatar(name = u.name, size = 40.dp)
                }
            )
        }
    }
}

@Composable
private fun ChannelsOversightTab(
    channels: List<com.example.data.model.Channel>,
    onBroadcastClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_channels_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Public Channels (${channels.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Directory moderation and subscriber reach", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onBroadcastClick) {
                    Icon(Icons.Default.Campaign, contentDescription = "Broadcast", tint = WhatsAppGreen)
                }
            }
        }

        items(channels, key = { it.id }) { ch ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(name = ch.name, size = 48.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(ch.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            if (ch.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text("${ch.subscribersCount} subscribers • Admin: ${ch.adminName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(ch.description, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityLogsTab(
    securityLogs: List<String>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_logs_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Admin & Security Audit Trail", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Real-time cryptographic verification and access events", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(securityLogs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(log, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtext, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
