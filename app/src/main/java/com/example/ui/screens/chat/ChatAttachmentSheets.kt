package com.example.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CoralRed
import com.example.ui.theme.WhatsAppGreen

@Composable
fun AttachmentOptionItem(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePollDialog(
    onDismiss: () -> Unit,
    onCreatePoll: (question: String, options: List<String>, allowMultiple: Boolean) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var opt1 by remember { mutableStateOf("") }
    var opt2 by remember { mutableStateOf("") }
    var opt3 by remember { mutableStateOf("") }
    var allowMultiple by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create In-Chat Poll") },
        text = {
            Column {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Ask a question...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = opt1,
                    onValueChange = { opt1 = it },
                    label = { Text("Option 1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = opt2,
                    onValueChange = { opt2 = it },
                    label = { Text("Option 2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = opt3,
                    onValueChange = { opt3 = it },
                    label = { Text("Option 3 (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Allow multiple answers", fontSize = 13.sp)
                    Switch(checked = allowMultiple, onCheckedChange = { allowMultiple = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (question.isNotBlank() && opt1.isNotBlank() && opt2.isNotBlank()) {
                        val opts = listOfNotNull(opt1.trim(), opt2.trim(), opt3.trim().ifEmpty { null })
                        onCreatePoll(question.trim(), opts, allowMultiple)
                    }
                }
            ) {
                Text("Send Poll")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ScheduleMessageDialog(
    onDismiss: () -> Unit,
    onSchedule: (timeFormatted: String) -> Unit
) {
    var selectedTime by remember { mutableStateOf("Tomorrow at 9:00 AM") }
    val times = listOf(
        "Later today at 8:00 PM",
        "Tomorrow at 9:00 AM",
        "Tomorrow at 1:00 PM",
        "Monday at 9:00 AM"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Message") },
        text = {
            Column {
                Text("Message will automatically be dispatched at the scheduled time:", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                times.forEach { t ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTime = t }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedTime == t, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSchedule(selectedTime) }) { Text("Schedule") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoSheet(
    chat: Chat,
    onDismiss: () -> Unit,
    onPromoteAdmin: (String) -> Unit,
    onDemoteAdmin: (String) -> Unit,
    onMuteMember: (String) -> Unit,
    onRemoveMember: (String) -> Unit,
    onApproveRequest: (String) -> Unit,
    onUpdatePermissions: (onlyAdminsSend: Boolean, onlyAdminsEdit: Boolean) -> Unit
) {
    var onlyAdminsSend by remember { mutableStateOf(chat.onlyAdminsCanSend) }
    var onlyAdminsEdit by remember { mutableStateOf(chat.onlyAdminsCanEditInfo) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(name = chat.name, avatarColor = chat.avatarColor, size = 56.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(chat.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${chat.groupMembersCount} members • Group link active", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(chat.groupDescription, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Pending approval requests
            if (chat.pendingJoinRequests.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text("Pending Join Requests (${chat.pendingJoinRequests.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WhatsAppGreen)
                chat.pendingJoinRequests.forEach { applicant ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(applicant, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Button(
                            onClick = { onApproveRequest(applicant) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Text("Approve", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Group Permissions", fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Only Admins can send messages", fontSize = 13.sp)
                Switch(
                    checked = onlyAdminsSend,
                    onCheckedChange = {
                        onlyAdminsSend = it
                        onUpdatePermissions(onlyAdminsSend, onlyAdminsEdit)
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Members & Roles", fontWeight = FontWeight.Bold, fontSize = 13.sp)

            chat.memberNames.forEach { member ->
                val isAdmin = chat.adminNames.contains(member)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(name = member, size = 32.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(member, fontSize = 13.sp, fontWeight = if (isAdmin) FontWeight.Bold else FontWeight.Normal)
                        if (isAdmin) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = WhatsAppGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                Text("Admin", color = WhatsAppGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }

                    if (member != "You") {
                        Row {
                            TextButton(onClick = { if (isAdmin) onDemoteAdmin(member) else onPromoteAdmin(member) }) {
                                Text(if (isAdmin) "Demote" else "Make Admin", fontSize = 11.sp)
                            }
                            IconButton(onClick = { onRemoveMember(member) }) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove", tint = CoralRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
