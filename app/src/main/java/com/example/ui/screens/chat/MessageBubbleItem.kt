package com.example.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.components.FullScreenMediaViewerDialog
import com.example.ui.components.MessageStatusIcon
import com.example.ui.theme.*
import com.example.util.MediaHelper

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MessageBubbleItem(
    message: Message,
    bubbleStyle: BubbleStyle,
    onLongClick: () -> Unit,
    onReactionClick: (String) -> Unit,
    onPollVote: (optionId: String) -> Unit,
    onEventRsvp: () -> Unit,
    onTranscribeVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOutgoing = message.isOutgoing
    val context = LocalContext.current
    var showFullscreenMedia by remember { mutableStateOf(false) }
    val alignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isOutgoing) {
        if (androidx.compose.foundation.isSystemInDarkTheme()) WhatsAppDarkBubble else WhatsAppLightGreen
    } else {
        if (androidx.compose.foundation.isSystemInDarkTheme()) DarkIncomingBubble else LightIncomingBubble
    }

    val bubbleShape = when (bubbleStyle) {
        BubbleStyle.ROUNDED_MODERN -> RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isOutgoing) 16.dp else 4.dp,
            bottomEnd = if (isOutgoing) 4.dp else 16.dp
        )
        BubbleStyle.WHATSAPP_CLASSIC -> RoundedCornerShape(
            topStart = 10.dp,
            topEnd = 10.dp,
            bottomStart = if (isOutgoing) 10.dp else 0.dp,
            bottomEnd = if (isOutgoing) 0.dp else 10.dp
        )
        BubbleStyle.PILL_SLEEK -> RoundedCornerShape(20.dp)
    }

    var showReactionDetailDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 90.dp, max = 310.dp)
                .combinedClickable(
                    onClick = { /* tap */ },
                    onLongClick = onLongClick
                )
                .testTag("msg_bubble_${message.id}"),
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                // Reply snapshot preview
                if (message.replyTo != null) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(modifier = Modifier.padding(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .background(WhatsAppGreen, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = message.replyTo.senderName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WhatsAppGreen
                                )
                                Text(
                                    text = message.replyTo.text,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // In-Chat Poll
                if (message.type == MessageType.POLL && message.pollData != null) {
                    val poll = message.pollData
                    val totalVotes = poll.options.sumOf { it.voterNames.size }.coerceAtLeast(1)

                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Poll, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(poll.question, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(
                            text = if (poll.allowMultipleAnswers) "Select one or more" else "Select one option",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        poll.options.forEach { opt ->
                            val voteCount = opt.voterNames.size
                            val hasVoted = opt.voterNames.contains("You")
                            val percentage = (voteCount.toFloat() / totalVotes)

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (hasVoted) WhatsAppGreen.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.05f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPollVote(opt.id) }
                                    .padding(vertical = 3.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (hasVoted) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Text(opt.text, fontSize = 13.sp, fontWeight = if (hasVoted) FontWeight.Bold else FontWeight.Normal)
                                        }
                                        Text("$voteCount votes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { percentage },
                                        color = WhatsAppGreen,
                                        trackColor = Color.LightGray.copy(alpha = 0.4f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }
                    }
                }
                // Group Event Card
                else if (message.type == MessageType.EVENT && message.eventData != null) {
                    val event = message.eventData
                    val isAttending = event.attendeeNames.contains("You")

                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(event.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(event.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("📅 ${event.dateTimeStr}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("📍 ${event.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onEventRsvp,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAttending) WhatsAppGreen else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(if (isAttending) Icons.Default.Check else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isAttending) "RSVP: Going (${event.attendeeNames.size})" else "RSVP Now", fontSize = 11.sp)
                        }
                    }
                }
                // Voice Note
                else if (message.type == MessageType.VOICE_NOTE) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Play",
                                tint = WhatsAppGreen,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                listOf(8, 16, 24, 12, 28, 18, 10, 22, 14, 20).forEach { h ->
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(h.dp)
                                            .background(WhatsAppGreen.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "0:${message.voiceDurationSeconds.coerceAtLeast(10)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Voice-to-Text Transcription feature
                        if (message.voiceTranscription != null) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.RecordVoiceOver, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Voice Transcription", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = WhatsAppGreen)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(message.voiceTranscription, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                }
                            }
                        } else {
                            TextButton(
                                onClick = onTranscribeVoice,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Outlined.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Transcribe Audio", fontSize = 11.sp)
                            }
                        }
                    }
                }
                // Image or Video with View Once & Gallery support
                else if (message.type == MessageType.IMAGE || message.type == MessageType.VIDEO) {
                    val isVideo = message.type == MessageType.VIDEO
                    Column(modifier = Modifier.widthIn(max = 260.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.08f))
                                .clickable { showFullscreenMedia = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (message.mediaUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = message.mediaUrl,
                                    contentDescription = message.mediaName.ifEmpty { if (isVideo) "Video" else "Photo" },
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Image,
                                        contentDescription = null,
                                        tint = WhatsAppGreen,
                                        modifier = Modifier.size(42.dp)
                                    )
                                    Text(
                                        text = message.mediaName.ifEmpty { if (isVideo) "Video" else "Photo" },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (isVideo) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            // View Once badge or download icon overlay
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (message.isViewOnce) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            "① View Once",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (message.mediaUrl.isNotEmpty()) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable {
                                                val saved = MediaHelper.saveMediaToGallery(
                                                    context,
                                                    message.mediaUrl,
                                                    isVideo
                                                )
                                                android.widget.Toast.makeText(
                                                    context,
                                                    if (saved) "Saved to phone gallery" else "Failed to save",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Download,
                                                contentDescription = "Save to phone",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Caption if any
                        if (message.text.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.text,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
                // Text Message (Mentions highlighted)
                else {
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Translated text view if translated
                if (message.translatedText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = message.translatedText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }

                // Time, Status, Star, Edited row
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.isSilent) {
                        Icon(Icons.Default.VolumeMute, contentDescription = "Silent", modifier = Modifier.size(12.dp).padding(end = 4.dp))
                    }
                    if (message.isEdited) {
                        Text(
                            text = "Edited",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    if (message.isStarred) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Starred",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier
                                .size(12.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = message.formattedTime,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MessageStatusIcon(status = message.status)
                    }
                }
            }
        }

        // Reaction Badges row under message bubble
        if (message.reactions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 2.dp, start = if (isOutgoing) 0.dp else 8.dp, end = if (isOutgoing) 8.dp else 0.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showReactionDetailDialog = true }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                message.reactions.forEach { (emoji, users) ->
                    Text(text = "$emoji ${users.size}", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 3.dp))
                }
            }
        }
    }

    if (showReactionDetailDialog) {
        AlertDialog(
            onDismissRequest = { showReactionDetailDialog = false },
            title = { Text("Reactions") },
            text = {
                Column {
                    message.reactions.forEach { (emoji, users) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(emoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(users.joinToString(", "), fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showReactionDetailDialog = false }) { Text("Close") }
            }
        )
    }

    if (showFullscreenMedia && (message.type == MessageType.IMAGE || message.type == MessageType.VIDEO)) {
        val uri = if (message.mediaUrl.isNotEmpty()) android.net.Uri.parse(message.mediaUrl) else null
        val isVideo = message.type == MessageType.VIDEO
        FullScreenMediaViewerDialog(
            mediaUrl = message.mediaUrl,
            mediaName = message.mediaName.ifEmpty { if (isVideo) "Video" else "Photo" },
            isVideo = isVideo,
            onDismiss = { showFullscreenMedia = false },
            onSaveToGallery = {
                val saved = MediaHelper.saveMediaToGallery(
                    context,
                    message.mediaUrl,
                    isVideo
                )
                android.widget.Toast.makeText(
                    context,
                    if (saved) "Saved to phone gallery" else "Failed to save",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            onShare = {
                MediaHelper.shareMedia(context, message.mediaUrl, message.text)
            }
        )
    }
}
