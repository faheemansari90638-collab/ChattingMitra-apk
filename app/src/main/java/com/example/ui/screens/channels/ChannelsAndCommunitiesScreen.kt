package com.example.ui.screens.channels

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
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Groups
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
import com.example.data.model.Channel
import com.example.data.model.Community
import com.example.ui.components.UserAvatar
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelsAndCommunitiesScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val channels by viewModel.channels.collectAsState()
    val communities by viewModel.communities.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Channels, 1 = Communities
    var showCreateChannelDialog by remember { mutableStateOf(false) }
    var newChannelName by remember { mutableStateOf("") }
    var newChannelDesc by remember { mutableStateOf("") }

    var showCreateCommunityDialog by remember { mutableStateOf(false) }
    var newCommName by remember { mutableStateOf("") }
    var newCommDesc by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Channels & Communities", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (selectedTabIndex == 0) showCreateChannelDialog = true
                            else showCreateCommunityDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Channels (${channels.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Communities (${communities.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (selectedTabIndex == 0) {
                // Channels Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("channels_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = WhatsAppGreen.copy(alpha = 0.12f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Discover Public Channels", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Stay updated on topics you care about. Channels are one-way broadcast feeds.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    items(channels, key = { it.id }) { channel ->
                        ChannelCard(
                            channel = channel,
                            onToggleFollow = { viewModel.chatRepo.toggleSubscribeChannel(channel.id) }
                        )
                    }
                }
            } else {
                // Communities Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("communities_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Diversity3, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Communities on ChatMitra", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Bring related sub-groups together under one umbrella organization.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    items(communities, key = { it.id }) { comm ->
                        CommunityCard(community = comm)
                    }
                }
            }
        }
    }

    // Create Channel Dialog
    if (showCreateChannelDialog) {
        AlertDialog(
            onDismissRequest = { showCreateChannelDialog = false },
            title = { Text("Create Channel") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newChannelName,
                        onValueChange = { newChannelName = it },
                        label = { Text("Channel Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newChannelDesc,
                        onValueChange = { newChannelDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newChannelName.isNotBlank()) {
                            viewModel.chatRepo.createChannel(newChannelName.trim(), newChannelDesc.trim(), true)
                            newChannelName = ""
                            newChannelDesc = ""
                            showCreateChannelDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateChannelDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Create Community Dialog
    if (showCreateCommunityDialog) {
        AlertDialog(
            onDismissRequest = { showCreateCommunityDialog = false },
            title = { Text("Create Community") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCommName,
                        onValueChange = { newCommName = it },
                        label = { Text("Community Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newCommDesc,
                        onValueChange = { newCommDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCommName.isNotBlank()) {
                            viewModel.chatRepo.createCommunity(newCommName.trim(), newCommDesc.trim(), emptyList())
                            newCommName = ""
                            newCommDesc = ""
                            showCreateCommunityDialog = false
                        }
                    }
                ) {
                    Text("Create Community")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCommunityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ChannelCard(
    channel: Channel,
    onToggleFollow: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(name = channel.name, avatarColor = channel.avatarColor, size = 46.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(channel.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${channel.subscribersCount} followers", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Button(
                    onClick = onToggleFollow,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (channel.isSubscribed) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else WhatsAppGreen,
                        contentColor = if (channel.isSubscribed) MaterialTheme.colorScheme.onSurface else Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(if (channel.isSubscribed) "Following" else "Follow", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(channel.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Latest Posts preview
            if (channel.posts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                channel.posts.take(2).forEach { post ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(post.text, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("👁️ ${post.viewsCount} views • 🔁 ${post.sharesCount} shares", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(post.time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityCard(community: Community) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(name = community.name, avatarColor = community.avatarColor, size = 48.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(community.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${community.membersCount} members", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(community.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))
            Text("Sub-groups inside this Community:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))

            community.groupNames.forEach { groupName ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(groupName, fontSize = 13.sp)
                }
            }
        }
    }
}
