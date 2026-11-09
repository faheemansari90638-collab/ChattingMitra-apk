package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Visibility
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
import com.example.data.model.UserStatusStory
import com.example.ui.components.UserAvatar
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesTab(
    viewModel: MainViewModel,
    onStatusClick: (String) -> Unit,
    onCreateStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val myStatus by viewModel.myStatus.collectAsState()
    val recentStatuses by viewModel.recentStatuses.collectAsState()
    val viewedStatuses by viewModel.viewedStatuses.collectAsState()

    var showViewersSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Status Title Header
        item {
            Text(
                text = "Status",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        // My Status Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (myStatus != null && myStatus!!.items.isNotEmpty()) {
                            onStatusClick("user_me")
                        } else {
                            onCreateStatusClick()
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("my_status_row"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (myStatus != null && myStatus!!.items.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, WhatsAppGreen, CircleShape)
                                .padding(3.dp)
                        ) {
                            UserAvatar(
                                name = "My Status",
                                avatarColor = 0xFF008069,
                                avatarUrl = currentUser.avatarUrl,
                                size = 48.dp
                            )
                        }
                    } else {
                        UserAvatar(
                            name = "You",
                            avatarColor = 0xFF008069,
                            avatarUrl = currentUser.avatarUrl,
                            size = 52.dp
                        )
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(WhatsAppGreen)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add status",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Status",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (myStatus != null && myStatus!!.items.isNotEmpty())
                            myStatus!!.items.last().formattedTime + " • Tap to view"
                        else
                            "Tap to add status update",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (myStatus != null && myStatus!!.items.isNotEmpty()) {
                    IconButton(
                        onClick = { showViewersSheet = true },
                        modifier = Modifier.testTag("status_viewers_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Visibility,
                                contentDescription = "Viewers",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${myStatus!!.items.first().viewers.size}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Row {
                        IconButton(onClick = onCreateStatusClick) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Text status",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 0.5.dp
            )
        }

        // Recent Updates Section
        if (recentStatuses.isNotEmpty()) {
            item {
                Text(
                    text = "Recent updates",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            items(recentStatuses, key = { it.userId }) { story ->
                StatusStoryItem(
                    story = story,
                    isViewed = false,
                    onClick = { onStatusClick(story.userId) }
                )
            }
        }

        // Viewed Updates Section
        if (viewedStatuses.isNotEmpty()) {
            item {
                Text(
                    text = "Viewed updates",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            items(viewedStatuses, key = { it.userId }) { story ->
                StatusStoryItem(
                    story = story,
                    isViewed = true,
                    onClick = { onStatusClick(story.userId) }
                )
            }
        }

        // Channels / Status Privacy Info Notice
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = WhatsAppGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Your status updates are end-to-end encrypted and automatically expire after 24 hours.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Viewers Bottom Sheet
    if (showViewersSheet && myStatus != null && myStatus!!.items.isNotEmpty()) {
        val viewers = myStatus!!.items.first().viewers
        ModalBottomSheet(onDismissRequest = { showViewersSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Viewed by ${viewers.size}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(
                        onClick = {
                            viewModel.statusRepo.deleteMyStatusItem(myStatus!!.items.first().id)
                            showViewersSheet = false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Status",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (viewers.isEmpty()) {
                    Text(
                        text = "No views yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    viewers.forEach { viewer ->
                        ListItem(
                            headlineContent = { Text(viewer.userName, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text(viewer.viewedTime, fontSize = 12.sp) },
                            leadingContent = {
                                UserAvatar(name = viewer.userName, size = 40.dp)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatusStoryItem(
    story: UserStatusStory,
    isViewed: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("status_item_${story.userId}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val ringColor = if (isViewed) Color.Gray.copy(alpha = 0.5f) else WhatsAppGreen
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(2.5.dp, ringColor, CircleShape)
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            UserAvatar(
                name = story.userName,
                avatarColor = story.avatarColor,
                size = 48.dp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = story.userName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = story.items.lastOrNull()?.formattedTime ?: "Today",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
