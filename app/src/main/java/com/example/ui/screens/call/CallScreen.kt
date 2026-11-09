package com.example.ui.screens.call

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ActiveCallStatus
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CoralRed
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CallScreen(
    isVideo: Boolean,
    viewModel: MainViewModel,
    onEndCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onEndCallClick() }

    val activeCall by viewModel.activeCall.collectAsState()
    var showAddParticipantDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val durationText = remember(activeCall.durationSeconds) {
        val mins = activeCall.durationSeconds / 60
        val secs = activeCall.durationSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1B21))
            .testTag("call_screen_container")
    ) {
        // Main Video or Audio Canvas
        if (activeCall.isVideo) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E2D38), Color(0xFF0B141B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // If Group Video Call: 2x2 Grid Layout
                if (activeCall.isGroupCall && activeCall.participants.size > 2) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(top = 80.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val peers = activeCall.participants.take(4)
                        peers.chunked(2).forEach { rowPeers ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowPeers.forEach { peerName ->
                                    val isSpeaking = activeCall.activeSpeaker == peerName
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF1E2C33),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(130.dp)
                                            .border(
                                                width = if (isSpeaking) 2.dp else 0.dp,
                                                color = if (isSpeaking) WhatsAppGreen else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                UserAvatar(name = peerName, size = 48.dp)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(peerName, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                            }
                                            if (isSpeaking) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(4.dp)
                                                        .clip(CircleShape)
                                                        .background(WhatsAppGreen)
                                                        .padding(3.dp)
                                                ) {
                                                    Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Single Peer HD Video Feed
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.scale(activeCall.zoomLevel)
                    ) {
                        UserAvatar(
                            name = activeCall.callerName,
                            avatarColor = activeCall.callerAvatarColor,
                            size = 120.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = if (activeCall.status == ActiveCallStatus.CONNECTED) "HD Video Connected" else "Connecting video feed...",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // PiP Local Camera Preview in Top Right
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF233138),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 16.dp)
                        .size(width = 96.dp, height = 136.dp)
                        .clickable { viewModel.callRepo.switchCamera() }
                        .testTag("pip_camera_preview")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (activeCall.isFrontCamera) Icons.Default.Face else Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (activeCall.isFrontCamera) "Front" else "Rear",
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlipCameraAndroid,
                                contentDescription = "Flip",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Audio Call Screen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .scale(if (activeCall.status == ActiveCallStatus.OUTGOING_RINGING) pulseScale else 1f)
                    ) {
                        UserAvatar(
                            name = activeCall.callerName,
                            avatarColor = activeCall.callerAvatarColor,
                            size = 130.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = if (activeCall.isGroupCall) "${activeCall.callerName} (${activeCall.participants.size})" else activeCall.callerName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (activeCall.status) {
                            ActiveCallStatus.OUTGOING_RINGING -> "Ringing..."
                            ActiveCallStatus.INCOMING_RINGING -> "Incoming call..."
                            ActiveCallStatus.CONNECTED -> durationText
                            ActiveCallStatus.ENDED -> "Call Ended"
                            else -> "Connecting..."
                        },
                        color = if (activeCall.status == ActiveCallStatus.CONNECTED) WhatsAppGreen else Color.LightGray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("End-to-end encrypted", color = Color.LightGray, fontSize = 12.sp)
                    }
                }
            }
        }

        // Top Info & Network Quality Banner
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 20.dp)
        ) {
            Text(
                text = activeCall.callerName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(WhatsAppGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${activeCall.networkQualityText} • ${durationText}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }

        // Call Waiting Floating Alert Banner
        if (activeCall.isCallWaitingActive) {
            Surface(
                color = Color(0xFF2A3942),
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Call Waiting", color = WhatsAppGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("${activeCall.callWaitingName} is calling...", color = Color.White, fontSize = 13.sp)
                    }
                    Row {
                        Button(
                            onClick = { viewModel.callRepo.switchCallWaiting() },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text("Switch", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(
                            onClick = { viewModel.callRepo.dismissCallWaiting() },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.LightGray)
                        ) {
                            Text("Dismiss", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Bottom Controls Bar
        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = Color(0xFF1B2831),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (activeCall.status == ActiveCallStatus.INCOMING_RINGING) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CallActionButton(icon = Icons.Default.CallEnd, label = "Decline", bgColor = CoralRed, iconColor = Color.White, onClick = onEndCallClick)
                        CallActionButton(icon = Icons.Default.Call, label = "Answer", bgColor = WhatsAppGreen, iconColor = Color.White, onClick = { viewModel.callRepo.acceptIncomingCall() })
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CallActionButton(
                            icon = if (activeCall.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            label = if (activeCall.isMuted) "Unmute" else "Mute",
                            bgColor = if (activeCall.isMuted) Color.White else Color(0xFF2C3E48),
                            iconColor = if (activeCall.isMuted) Color.Black else Color.White,
                            onClick = { viewModel.callRepo.toggleMute() }
                        )

                        CallActionButton(
                            icon = if (activeCall.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            label = "Speaker",
                            bgColor = if (activeCall.isSpeakerOn) Color.White else Color(0xFF2C3E48),
                            iconColor = if (activeCall.isSpeakerOn) Color.Black else Color.White,
                            onClick = { viewModel.callRepo.toggleSpeaker() }
                        )

                        // Add Participant to group call
                        CallActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Add",
                            bgColor = Color(0xFF2C3E48),
                            iconColor = Color.White,
                            onClick = { showAddParticipantDialog = true }
                        )

                        if (activeCall.isVideo) {
                            CallActionButton(
                                icon = Icons.Default.FlipCameraAndroid,
                                label = "Flip",
                                bgColor = Color(0xFF2C3E48),
                                iconColor = Color.White,
                                onClick = { viewModel.callRepo.switchCamera() }
                            )
                        }

                        CallActionButton(
                            icon = Icons.Default.CallEnd,
                            label = "End",
                            bgColor = CoralRed,
                            iconColor = Color.White,
                            onClick = onEndCallClick
                        )
                    }
                }
            }
        }
    }

    if (showAddParticipantDialog) {
        AlertDialog(
            onDismissRequest = { showAddParticipantDialog = false },
            title = { Text("Add to Call") },
            text = {
                Column {
                    listOf("Aarav Sharma", "Priya Patel", "Rohan Verma", "Sneha Kulkarni").forEach { name ->
                        ListItem(
                            headlineContent = { Text(name) },
                            leadingContent = { UserAvatar(name = name, size = 32.dp) },
                            modifier = Modifier.clickable {
                                viewModel.callRepo.addParticipant(name)
                                showAddParticipantDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddParticipantDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(bgColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp)
    }
}
