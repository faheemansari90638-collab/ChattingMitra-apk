package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.CallLog
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CoralRed
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CallsTab(
    viewModel: MainViewModel,
    onStartCall: (name: String, phone: String, avatarColor: Long, isVideo: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val callLogs by viewModel.callLogs.collectAsState()
    var showLinkDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Create Call Link Item
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLinkDialog = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("create_call_link_row"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(WhatsAppGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Create call link",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Create call link",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Share a link for your ChatMitra call",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 0.5.dp
            )
        }

        // Recent Calls Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (callLogs.isNotEmpty()) {
                    TextButton(onClick = { viewModel.callRepo.clearCallLogs() }) {
                        Text("Clear", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    }
                }
            }
        }

        if (callLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recent calls",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(callLogs, key = { it.id }) { log ->
                CallLogRow(
                    log = log,
                    onCallClick = { isVideo ->
                        onStartCall(log.callerName, log.callerPhone, log.callerAvatarColor, isVideo)
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    thickness = 0.5.dp
                )
            }
        }
    }

    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            title = { Text("ChatMitra Call Link") },
            text = {
                Column {
                    Text(
                        "Anyone with ChatMitra can use this link to join this call:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "https://chatmitra.app/call/m-${System.currentTimeMillis() % 100000}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showLinkDialog = false }) {
                    Text("Copy Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun CallLogRow(
    log: CallLog,
    onCallClick: (isVideo: Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCallClick(log.isVideo) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("call_log_${log.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            name = log.callerName,
            avatarColor = log.callerAvatarColor,
            size = 50.dp
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.callerName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = if (log.isMissed) CoralRed else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val arrowIcon = when {
                    log.isMissed -> Icons.AutoMirrored.Filled.CallMissed
                    log.isIncoming -> Icons.AutoMirrored.Filled.CallReceived
                    else -> Icons.AutoMirrored.Filled.CallMade
                }
                val arrowColor = if (log.isMissed) CoralRed else WhatsAppGreen

                Icon(
                    imageVector = arrowIcon,
                    contentDescription = null,
                    tint = arrowColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${log.formattedDate} • ${log.durationText}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(
            onClick = { onCallClick(log.isVideo) },
            modifier = Modifier.testTag("call_action_btn_${log.id}")
        ) {
            Icon(
                imageVector = if (log.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = if (log.isVideo) "Video call" else "Voice call",
                tint = WhatsAppGreen,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
