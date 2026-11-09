package com.example.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
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
import com.example.ui.components.AdMobBannerSimulator
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HomeTab
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentHomeTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val allChats by viewModel.allChats.collectAsState()
    val settings by viewModel.appSettings.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showNewGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    var showCameraSimDialog by remember { mutableStateOf(false) }

    val totalUnread = allChats.sumOf { it.unreadCount }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        if (isSearchActive) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Search chats, messages...") },
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            viewModel.setSearchQuery("")
                                            isSearchActive = false
                                        }
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close search")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("home_search_bar")
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ChatMitra",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = WhatsAppGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "3D",
                                        color = WhatsAppGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        if (!isSearchActive) {
                            IconButton(
                                onClick = { showCameraSimDialog = true },
                                modifier = Modifier.testTag("topbar_camera_btn")
                            ) {
                                Icon(Icons.Outlined.CameraAlt, contentDescription = "Camera")
                            }
                            IconButton(
                                onClick = { isSearchActive = true },
                                modifier = Modifier.testTag("topbar_search_btn")
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                            Box {
                                IconButton(
                                    onClick = { showOverflowMenu = true },
                                    modifier = Modifier.testTag("topbar_menu_btn")
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                                }

                                DropdownMenu(
                                    expanded = showOverflowMenu,
                                    onDismissRequest = { showOverflowMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("New group") },
                                        leadingIcon = { Icon(Icons.Default.GroupAdd, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            showNewGroupDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Channels & Communities") },
                                        leadingIcon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.navigateTo(AppScreen.ChannelsAndCommunities)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Linked devices") },
                                        leadingIcon = { Icon(Icons.Default.Devices, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.navigateTo(AppScreen.LinkedDevices)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Smart Storage") },
                                        leadingIcon = { Icon(Icons.Default.CleaningServices, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.navigateTo(AppScreen.SmartStorage)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Security Center") },
                                        leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.navigateTo(AppScreen.SecurityCenter)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Starred messages") },
                                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.setChatFilter("Favorites")
                                            viewModel.setHomeTab(HomeTab.CHATS)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Admin Portal") },
                                        leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.navigateTo(AppScreen.AdminDashboard)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Settings") },
                                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            viewModel.setHomeTab(HomeTab.SETTINGS)
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // AdMob simulator banner
                AdMobBannerSimulator(
                    isPremium = settings.isPremium,
                    onUpgradeClick = { viewModel.navigateTo(AppScreen.Premium) }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == HomeTab.CHATS,
                    onClick = { viewModel.setHomeTab(HomeTab.CHATS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (totalUnread > 0) {
                                    Badge(containerColor = WhatsAppGreen) {
                                        Text("$totalUnread", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == HomeTab.CHATS) Icons.AutoMirrored.Filled.Message else Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Chats"
                            )
                        }
                    },
                    label = { Text("Chats", fontWeight = if (currentTab == HomeTab.CHATS) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_tab_chats")
                )

                NavigationBarItem(
                    selected = currentTab == HomeTab.UPDATES,
                    onClick = { viewModel.setHomeTab(HomeTab.UPDATES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == HomeTab.UPDATES) Icons.Default.ChangeCircle else Icons.Outlined.ChangeCircle,
                            contentDescription = "Updates"
                        )
                    },
                    label = { Text("Updates", fontWeight = if (currentTab == HomeTab.UPDATES) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_tab_updates")
                )

                NavigationBarItem(
                    selected = currentTab == HomeTab.CALLS,
                    onClick = { viewModel.setHomeTab(HomeTab.CALLS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == HomeTab.CALLS) Icons.Default.Call else Icons.Outlined.Call,
                            contentDescription = "Calls"
                        )
                    },
                    label = { Text("Calls", fontWeight = if (currentTab == HomeTab.CALLS) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_tab_calls")
                )

                NavigationBarItem(
                    selected = currentTab == HomeTab.CONTACTS,
                    onClick = { viewModel.setHomeTab(HomeTab.CONTACTS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == HomeTab.CONTACTS) Icons.Default.People else Icons.Outlined.PeopleOutline,
                            contentDescription = "Contacts"
                        )
                    },
                    label = { Text("Contacts", fontWeight = if (currentTab == HomeTab.CONTACTS) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_tab_contacts")
                )

                NavigationBarItem(
                    selected = currentTab == HomeTab.SETTINGS,
                    onClick = { viewModel.setHomeTab(HomeTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == HomeTab.SETTINGS) Icons.Default.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings", fontWeight = if (currentTab == HomeTab.SETTINGS) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        },
        floatingActionButton = {
            when (currentTab) {
                HomeTab.CHATS -> {
                    FloatingActionButton(
                        onClick = { viewModel.setHomeTab(HomeTab.CONTACTS) },
                        containerColor = WhatsAppGreen,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_new_chat")
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "New chat")
                    }
                }
                HomeTab.UPDATES -> {
                    Column(horizontalAlignment = Alignment.End) {
                        SmallFloatingActionButton(
                            onClick = { viewModel.navigateTo(AppScreen.CreateStatus) },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .testTag("fab_text_status")
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Text status")
                        }
                        FloatingActionButton(
                            onClick = { viewModel.navigateTo(AppScreen.CreateStatus) },
                            containerColor = WhatsAppGreen,
                            contentColor = Color.White,
                            modifier = Modifier.testTag("fab_photo_status")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera status")
                        }
                    }
                }
                HomeTab.CALLS -> {
                    FloatingActionButton(
                        onClick = { viewModel.setHomeTab(HomeTab.CONTACTS) },
                        containerColor = WhatsAppGreen,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_new_call")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "New call")
                    }
                }
                HomeTab.CONTACTS -> {
                    FloatingActionButton(
                        onClick = { viewModel.setHomeTab(HomeTab.CONTACTS) },
                        containerColor = WhatsAppGreen,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_add_contact")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Contact")
                    }
                }
                else -> {}
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                HomeTab.CHATS -> {
                    ChatsTab(
                        viewModel = viewModel,
                        onChatClick = { chatId -> viewModel.navigateTo(AppScreen.ChatDetail(chatId)) }
                    )
                }
                HomeTab.UPDATES -> {
                    UpdatesTab(
                        viewModel = viewModel,
                        onStatusClick = { userId -> viewModel.navigateTo(AppScreen.StatusViewer(userId)) },
                        onCreateStatusClick = { viewModel.navigateTo(AppScreen.CreateStatus) }
                    )
                }
                HomeTab.CALLS -> {
                    CallsTab(
                        viewModel = viewModel,
                        onStartCall = { name, phone, color, isVideo ->
                            viewModel.startCall(name, phone, color, isVideo)
                        }
                    )
                }
                HomeTab.CONTACTS -> {
                    ContactsTab(
                        viewModel = viewModel,
                        onContactClick = { name, phone ->
                            // Find or create chat
                            val existing = allChats.find { it.name.equals(name, ignoreCase = true) }
                            if (existing != null) {
                                viewModel.navigateTo(AppScreen.ChatDetail(existing.id))
                            } else {
                                viewModel.chatRepo.addContactChat(name, phone)
                                val created = viewModel.allChats.value.first()
                                viewModel.navigateTo(AppScreen.ChatDetail(created.id))
                            }
                        },
                        onStartCall = { name, phone, color, isVideo ->
                            viewModel.startCall(name, phone, color, isVideo)
                        }
                    )
                }
                HomeTab.SETTINGS -> {
                    SettingsTab(viewModel = viewModel)
                }
            }
        }
    }

    // New Group Dialog
    if (showNewGroupDialog) {
        AlertDialog(
            onDismissRequest = { showNewGroupDialog = false },
            title = { Text("Create New Group") },
            text = {
                Column {
                    Text("Enter group subject and add members to collaborate:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text("Group Subject") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGroupName.isNotBlank()) {
                            viewModel.chatRepo.createGroupChat(newGroupName.trim(), 4)
                            newGroupName = ""
                            showNewGroupDialog = false
                        }
                    }
                ) {
                    Text("Create Group")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewGroupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Camera action preview dialog
    if (showCameraSimDialog) {
        AlertDialog(
            onDismissRequest = { showCameraSimDialog = false },
            title = { Text("ChatMitra Camera") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Camera Viewfinder Active", color = Color.White, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Take a photo or record video to share on ChatMitra Status or Chats.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCameraSimDialog = false
                        viewModel.statusRepo.addMyTextStatus("Captured new photo with ChatMitra Camera 📸", 0xFF008069)
                        viewModel.setHomeTab(HomeTab.UPDATES)
                    }
                ) {
                    Text("Capture & Post Status")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCameraSimDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
