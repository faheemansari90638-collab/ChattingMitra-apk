package com.example.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ProfilePhotoEditorDialog
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CoralRed
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.MediaHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedProfileScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val settings by viewModel.appSettings.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(currentUser.name) }

    var showEditBioDialog by remember { mutableStateOf(false) }
    var editBio by remember { mutableStateOf(currentUser.aboutBio) }

    var showStatusPickerDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var showDpPrivacyDialog by remember { mutableStateOf(false) }

    var showChangePhotoSheet by remember { mutableStateOf(false) }
    var selectedPhotoForEdit by remember { mutableStateOf<Uri?>(null) }
    var showPhotoEditorDialog by remember { mutableStateOf(false) }

    // Native Photo Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoForEdit = uri
            showPhotoEditorDialog = true
        }
    }

    // Native Camera capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val uri = MediaHelper.saveBitmapToAppStorage(context, bitmap, "camera_dp")
            selectedPhotoForEdit = uri
            showPhotoEditorDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Display Picture", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showQrDialog = true }) {
                        Icon(Icons.Default.QrCode, contentDescription = "My QR Code")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("advanced_profile_list"),
            contentPadding = PaddingValues(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Avatars Header with DP change action
            item {
                Box(
                    modifier = Modifier.clickable { showChangePhotoSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    UserAvatar(
                        name = currentUser.name,
                        avatarColor = currentUser.avatarColor,
                        avatarUrl = currentUser.avatarUrl,
                        size = 120.dp,
                        isOnline = true
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(WhatsAppGreen)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .clickable { showChangePhotoSheet = true }
                            .testTag("change_profile_photo_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change DP",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showChangePhotoSheet = true }) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change Photo", fontWeight = FontWeight.Bold)
                    }
                    if (currentUser.avatarUrl.isNotBlank()) {
                        TextButton(
                            onClick = { viewModel.chatRepo.removeUserProfilePicture() },
                            colors = ButtonDefaults.textButtonColors(contentColor = CoralRed)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(currentUser.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    if (currentUser.isVerified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified account",
                            tint = Color(0xFF1DA1F2),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(currentUser.profileHandle, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                Text(currentUser.phoneNumber, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Custom Online Status Pill
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable { showStatusPickerDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(currentUser.customStatusEmoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Status: ${currentUser.customStatusLabel}",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // DP Privacy Settings Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDpPrivacyDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Profile Photo Privacy", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Visible to: ${settings.profilePhotoPrivacy}", fontSize = 12.sp, color = WhatsAppGreen, fontWeight = FontWeight.SemiBold)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // About & Bio Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("About & Bio", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            IconButton(onClick = {
                                editBio = currentUser.aboutBio
                                showEditBioDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit bio", modifier = Modifier.size(18.dp))
                            }
                        }
                        Text(currentUser.aboutBio, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Display Name", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            IconButton(onClick = {
                                editName = currentUser.name
                                showEditNameDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit name", modifier = Modifier.size(18.dp))
                            }
                        }
                        Text(currentUser.name, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Verification Request Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentUser.isVerified) WhatsAppGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = if (currentUser.isVerified) WhatsAppGreen else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Account Verification", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (currentUser.isVerified)
                                "Your account has been verified as authentic by the ChatMitra trust team."
                            else
                                "Apply for a blue verified checkmark badge to certify your public identity on ChatMitra.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!currentUser.isVerified) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showVerificationDialog = true },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Request Verification", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Change Photo Source Dialog / Bottom Sheet
    if (showChangePhotoSheet) {
        AlertDialog(
            onDismissRequest = { showChangePhotoSheet = false },
            title = { Text("Profile Photo") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Select from Phone Gallery", fontWeight = FontWeight.SemiBold) },
                        leadingContent = {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = WhatsAppGreen)
                        },
                        modifier = Modifier.clickable {
                            showChangePhotoSheet = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Take New Photo with Camera", fontWeight = FontWeight.SemiBold) },
                        leadingContent = {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.clickable {
                            showChangePhotoSheet = false
                            cameraLauncher.launch(null)
                        }
                    )

                    if (currentUser.avatarUrl.isNotBlank()) {
                        ListItem(
                            headlineContent = { Text("Remove Profile Photo", color = CoralRed, fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = CoralRed)
                            },
                            modifier = Modifier.clickable {
                                viewModel.chatRepo.removeUserProfilePicture()
                                showChangePhotoSheet = false
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showChangePhotoSheet = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Photo Crop, Rotate & Zoom Editor Dialog
    if (showPhotoEditorDialog && selectedPhotoForEdit != null) {
        ProfilePhotoEditorDialog(
            photoUri = selectedPhotoForEdit!!,
            onDismiss = {
                showPhotoEditorDialog = false
                selectedPhotoForEdit = null
            },
            onConfirm = { finalUri ->
                viewModel.chatRepo.updateUserProfilePicture(finalUri)
                showPhotoEditorDialog = false
                selectedPhotoForEdit = null
            }
        )
    }

    // Profile Photo Privacy Selection Dialog
    if (showDpPrivacyDialog) {
        val options = listOf("Everyone", "My Contacts", "Nobody")
        AlertDialog(
            onDismissRequest = { showDpPrivacyDialog = false },
            title = { Text("Who can see my profile photo?") },
            text = {
                Column {
                    options.forEach { opt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.settingsRepo.setPrivacy(
                                        settings.lastSeenPrivacy,
                                        opt,
                                        settings.statusPrivacy
                                    )
                                    showDpPrivacyDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = settings.profilePhotoPrivacy == opt,
                                onClick = {
                                    viewModel.settingsRepo.setPrivacy(
                                        settings.lastSeenPrivacy,
                                        opt,
                                        settings.statusPrivacy
                                    )
                                    showDpPrivacyDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(opt, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDpPrivacyDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Display Name") },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (editName.isNotBlank()) {
                        viewModel.chatRepo.updateCurrentUser(editName.trim(), currentUser.statusText, currentUser.phoneNumber, currentUser.aboutBio)
                        showEditNameDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Edit Bio Dialog
    if (showEditBioDialog) {
        AlertDialog(
            onDismissRequest = { showEditBioDialog = false },
            title = { Text("Edit Bio") },
            text = {
                OutlinedTextField(
                    value = editBio,
                    onValueChange = { editBio = it },
                    label = { Text("About Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.chatRepo.updateCurrentUser(currentUser.name, currentUser.statusText, currentUser.phoneNumber, editBio.trim())
                    showEditBioDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBioDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Custom Status Picker
    if (showStatusPickerDialog) {
        val statusList = listOf(
            "🚀" to "Building",
            "💻" to "In a meeting",
            "☕" to "Taking a coffee break",
            "✈️" to "Traveling",
            "🏋️" to "At the gym",
            "🎧" to "Focus mode",
            "😴" to "Sleeping"
        )
        AlertDialog(
            onDismissRequest = { showStatusPickerDialog = false },
            title = { Text("Choose Custom Status") },
            text = {
                Column {
                    statusList.forEach { (emoji, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.chatRepo.setCustomStatus(emoji, label)
                                    showStatusPickerDialog = false
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Text(emoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(label, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStatusPickerDialog = false }) { Text("Cancel") }
            }
        )
    }

    // QR Code Dialog
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("Share Profile Link & QR") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(160.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color.Black, modifier = Modifier.size(140.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "https://chatmitra.app/u/${currentUser.profileHandle.removePrefix("@")}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showQrDialog = false }) { Text("Copy Link") }
            },
            dismissButton = {
                TextButton(onClick = { showQrDialog = false }) { Text("Close") }
            }
        )
    }

    // Verification Dialog
    if (showVerificationDialog) {
        AlertDialog(
            onDismissRequest = { showVerificationDialog = false },
            title = { Text("Apply for Verified Badge") },
            text = {
                Text("ChatMitra verified badges show that an account has been verified as authentic for public figures, creators, and business accounts.\n\nWould you like to submit your verification request?")
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.chatRepo.requestVerification()
                    showVerificationDialog = false
                }) {
                    Text("Submit Application")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVerificationDialog = false }) { Text("Cancel") }
            }
        )
    }
}
