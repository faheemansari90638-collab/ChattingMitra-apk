package com.example.ui.screens.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Country
import com.example.data.model.SampleCountries
import com.example.data.repository.*
import com.example.ui.components.ProfilePhotoEditorDialog
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.MediaHelper
import kotlinx.coroutines.delay

enum class AuthMode {
    REGISTER,
    LOGIN,
    RECOVER
}

enum class AuthStep {
    PHONE_INPUT,
    OTP_VERIFICATION,
    PROFILE_SETUP,
    RECOVERY_SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var authMode by remember { mutableStateOf(AuthMode.REGISTER) }
    var step by remember { mutableStateOf(AuthStep.PHONE_INPUT) }

    // Country selection
    var selectedCountry by remember {
        mutableStateOf(AuthRepository.detectCountry(context))
    }
    var showCountryPicker by remember { mutableStateOf(false) }
    var countrySearchQuery by remember { mutableStateOf("") }

    // Form fields
    var phoneNumber by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var statusNotice by remember { mutableStateOf<String?>(null) }

    // OTP state
    var currentVerificationId by remember { mutableStateOf("") }
    var otpDigits by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf<String?>(null) }
    var attemptsRemaining by remember { mutableIntStateOf(5) }
    var resendCooldown by remember { mutableIntStateOf(60) }
    var isVerifying by remember { mutableStateOf(false) }

    // Profile setup state (for new user)
    var fullName by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("Hey there! I am using ChatMitra 🚀") }
    var emailAddress by remember { mutableStateOf("") }
    var profileAvatarUrl by remember { mutableStateOf("") }

    // Profile photo editor state
    var showChangePhotoSheet by remember { mutableStateOf(false) }
    var selectedPhotoForEdit by remember { mutableStateOf<Uri?>(null) }
    var showPhotoEditorDialog by remember { mutableStateOf(false) }

    // Recovered account data
    var recoveredAccount by remember { mutableStateOf<RegisteredAccount?>(null) }
    var logoutOtherDevicesOnRecovery by remember { mutableStateOf(true) }

    // Incoming SMS notification banner for testing
    val incomingSms by viewModel.authRepo.incomingSms.collectAsState()

    // Photo picker & camera launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoForEdit = uri
            showPhotoEditorDialog = true
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val uri = MediaHelper.saveBitmapToAppStorage(context, bitmap, "dp_setup")
            selectedPhotoForEdit = uri
            showPhotoEditorDialog = true
        }
    }

    // Resend countdown timer
    LaunchedEffect(step, resendCooldown) {
        if (step == AuthStep.OTP_VERIFICATION && resendCooldown > 0) {
            delay(1000L)
            resendCooldown -= 1
        }
    }

    val fullPhoneNumber = "${selectedCountry.code} ${phoneNumber.trim()}"

    fun triggerSendOtp() {
        phoneError = null
        statusNotice = null
        val validationErr = viewModel.authRepo.validatePhoneNumber(phoneNumber.trim())
        if (validationErr != null) {
            phoneError = validationErr
            return
        }

        val result = viewModel.authRepo.sendOtp(
            countryCode = selectedCountry.code,
            nationalNumber = phoneNumber.trim(),
            mode = authMode.name
        )

        when (result) {
            is SendOtpResult.Success -> {
                currentVerificationId = result.verificationId
                resendCooldown = 60
                attemptsRemaining = 5
                otpDigits = ""
                otpError = null
                step = AuthStep.OTP_VERIFICATION
            }
            is SendOtpResult.Error -> {
                phoneError = result.message
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (step) {
                            AuthStep.PHONE_INPUT -> when (authMode) {
                                AuthMode.REGISTER -> "Register Account"
                                AuthMode.LOGIN -> "Log In to ChatMitra"
                                AuthMode.RECOVER -> "Recover Account"
                            }
                            AuthStep.OTP_VERIFICATION -> "Verify Phone Number"
                            AuthStep.PROFILE_SETUP -> "Profile Information"
                            AuthStep.RECOVERY_SUCCESS -> "Account Restored"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (step != AuthStep.PHONE_INPUT && step != AuthStep.RECOVERY_SUCCESS) {
                        IconButton(onClick = {
                            if (step == AuthStep.OTP_VERIFICATION) {
                                step = AuthStep.PHONE_INPUT
                            } else if (step == AuthStep.PROFILE_SETUP) {
                                step = AuthStep.OTP_VERIFICATION
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Simulated Incoming SMS Push Notification Banner
            incomingSms?.let { sms ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("sms_notification_banner"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SMS: ChatMitra Verification Code",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Your ChatMitra security code is: ${sms.code}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (step == AuthStep.OTP_VERIFICATION) {
                            Button(
                                onClick = {
                                    otpDigits = sms.code
                                    viewModel.authRepo.dismissIncomingSms()
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Auto-Fill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                when (step) {
                    AuthStep.PHONE_INPUT -> {
                        PhoneInputView(
                            authMode = authMode,
                            onAuthModeChange = {
                                authMode = it
                                phoneError = null
                                statusNotice = null
                            },
                            selectedCountry = selectedCountry,
                            onPickCountryClick = { showCountryPicker = true },
                            phoneNumber = phoneNumber,
                            onPhoneNumberChange = {
                                if (it.length <= 15) {
                                    phoneNumber = it
                                    phoneError = null
                                }
                            },
                            phoneError = phoneError,
                            statusNotice = statusNotice,
                            onContinueClick = {
                                keyboardController?.hide()
                                triggerSendOtp()
                            }
                        )
                    }

                    AuthStep.OTP_VERIFICATION -> {
                        OtpVerificationView(
                            fullPhone = fullPhoneNumber,
                            otpDigits = otpDigits,
                            onOtpChange = {
                                if (it.length <= 6) {
                                    otpDigits = it
                                    otpError = null
                                }
                            },
                            otpError = otpError,
                            resendCooldown = resendCooldown,
                            attemptsRemaining = attemptsRemaining,
                            isVerifying = isVerifying,
                            onResendClick = {
                                triggerSendOtp()
                            },
                            onVerifyClick = {
                                keyboardController?.hide()
                                if (otpDigits.length == 6) {
                                    isVerifying = true
                                    val verifyResult = viewModel.authRepo.verifyOtp(
                                        verificationId = currentVerificationId,
                                        enteredCode = otpDigits
                                    )
                                    isVerifying = false

                                    when (verifyResult) {
                                        is VerifyOtpResult.Success -> {
                                            if (verifyResult.isNewUser) {
                                                step = AuthStep.PROFILE_SETUP
                                            } else {
                                                val account = verifyResult.account!!
                                                if (authMode == AuthMode.RECOVER) {
                                                    recoveredAccount = account
                                                    step = AuthStep.RECOVERY_SUCCESS
                                                } else {
                                                    viewModel.loginExisting(account)
                                                }
                                            }
                                        }
                                        is VerifyOtpResult.Error -> {
                                            otpError = verifyResult.message
                                            attemptsRemaining = verifyResult.attemptsLeft
                                        }
                                    }
                                } else {
                                    otpError = "Please enter all 6 digits of the verification code."
                                }
                            },
                            onChangeNumberClick = {
                                step = AuthStep.PHONE_INPUT
                            }
                        )
                    }

                    AuthStep.PROFILE_SETUP -> {
                        ProfileSetupView(
                            fullName = fullName,
                            onFullNameChange = { fullName = it },
                            statusText = statusText,
                            onStatusChange = { statusText = it },
                            emailAddress = emailAddress,
                            onEmailChange = { emailAddress = it },
                            avatarUrl = profileAvatarUrl,
                            onChangePhotoClick = { showChangePhotoSheet = true },
                            onFinishClick = {
                                keyboardController?.hide()
                                if (fullName.isNotBlank()) {
                                    viewModel.registerAndLogin(
                                        fullPhone = fullPhoneNumber,
                                        countryCode = selectedCountry.code,
                                        nationalNumber = phoneNumber.trim(),
                                        name = fullName.trim(),
                                        statusText = statusText.trim(),
                                        email = emailAddress.trim(),
                                        avatarUrl = profileAvatarUrl
                                    )
                                }
                            }
                        )
                    }

                    AuthStep.RECOVERY_SUCCESS -> {
                        RecoverySuccessView(
                            account = recoveredAccount,
                            logoutOtherDevices = logoutOtherDevicesOnRecovery,
                            onLogoutOtherDevicesChange = { logoutOtherDevicesOnRecovery = it },
                            onContinueClick = {
                                recoveredAccount?.let { acc ->
                                    viewModel.recoverAccount(acc, logoutOtherDevicesOnRecovery)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Country Picker Modal Dialog
    if (showCountryPicker) {
        AlertDialog(
            onDismissRequest = {
                showCountryPicker = false
                countrySearchQuery = ""
            },
            title = {
                Column {
                    Text("Select your Country", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = countrySearchQuery,
                        onValueChange = { countrySearchQuery = it },
                        placeholder = { Text("Search by country or code...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            text = {
                val filteredCountries = SampleCountries.filter {
                    countrySearchQuery.isBlank() ||
                    it.name.contains(countrySearchQuery, ignoreCase = true) ||
                    it.code.contains(countrySearchQuery)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    items(filteredCountries) { country ->
                        ListItem(
                            headlineContent = { Text(country.name, fontWeight = FontWeight.Medium) },
                            leadingContent = { Text(country.flagEmoji, fontSize = 24.sp) },
                            trailingContent = { Text(country.code, fontWeight = FontWeight.Bold, color = WhatsAppGreen) },
                            modifier = Modifier.clickable {
                                selectedCountry = country
                                showCountryPicker = false
                                countrySearchQuery = ""
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    showCountryPicker = false
                    countrySearchQuery = ""
                }) {
                    Text("Close")
                }
            }
        )
    }

    // Photo Source Picker Sheet for Profile Setup
    if (showChangePhotoSheet) {
        AlertDialog(
            onDismissRequest = { showChangePhotoSheet = false },
            title = { Text("Select Profile Photo") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Choose from Phone Gallery", fontWeight = FontWeight.SemiBold) },
                        leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = WhatsAppGreen) },
                        modifier = Modifier.clickable {
                            showChangePhotoSheet = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Take New Photo with Camera", fontWeight = FontWeight.SemiBold) },
                        leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable {
                            showChangePhotoSheet = false
                            cameraLauncher.launch(null)
                        }
                    )
                    if (profileAvatarUrl.isNotBlank()) {
                        ListItem(
                            headlineContent = { Text("Remove Photo", color = CoralRed, fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = CoralRed) },
                            modifier = Modifier.clickable {
                                profileAvatarUrl = ""
                                showChangePhotoSheet = false
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showChangePhotoSheet = false }) { Text("Cancel") }
            }
        )
    }

    // Photo Crop, Rotate & Zoom Editor
    if (showPhotoEditorDialog && selectedPhotoForEdit != null) {
        ProfilePhotoEditorDialog(
            photoUri = selectedPhotoForEdit!!,
            onDismiss = {
                showPhotoEditorDialog = false
                selectedPhotoForEdit = null
            },
            onConfirm = { savedUri ->
                profileAvatarUrl = savedUri
                showPhotoEditorDialog = false
                selectedPhotoForEdit = null
            }
        )
    }
}

@Composable
private fun PhoneInputView(
    authMode: AuthMode,
    onAuthModeChange: (AuthMode) -> Unit,
    selectedCountry: Country,
    onPickCountryClick: () -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    phoneError: String?,
    statusNotice: String?,
    onContinueClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            // ChatMitra Logo & Branding
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(WhatsAppGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "ChatMitra Logo",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ChatMitra",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Private • Encrypted • HD Voice & Video",
                fontSize = 12.sp,
                color = WhatsAppGreen,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Auth Mode Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AuthMode.values().forEach { mode ->
                    val isSelected = authMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) WhatsAppGreen else Color.Transparent)
                            .clickable { onAuthModeChange(mode) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (mode) {
                                AuthMode.REGISTER -> "Register"
                                AuthMode.LOGIN -> "Log In"
                                AuthMode.RECOVER -> "Recover"
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = when (authMode) {
                    AuthMode.REGISTER -> "Enter your mobile number to register your new ChatMitra account. We will send an SMS verification code."
                    AuthMode.LOGIN -> "Enter your registered mobile number to receive an SMS OTP and securely log in."
                    AuthMode.RECOVER -> "Enter your registered mobile number to verify your identity and restore your profile & backups."
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Country Selector Card
            OutlinedCard(
                onClick = onPickCountryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("country_selector_card"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedCountry.flagEmoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = selectedCountry.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedCountry.code,
                            fontWeight = FontWeight.Bold,
                            color = WhatsAppGreen
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mobile Number Input Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = selectedCountry.code,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.width(82.dp),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    label = { Text("Mobile number") },
                    placeholder = { Text("98765 43210") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onContinueClick() }),
                    isError = phoneError != null,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("auth_phone_input")
                )
            }

            if (phoneError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = phoneError,
                    color = CoralRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("auth_send_otp_btn"),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
            ) {
                Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (authMode) {
                        AuthMode.REGISTER -> "Send Registration OTP"
                        AuthMode.LOGIN -> "Send Login OTP"
                        AuthMode.RECOVER -> "Verify & Recover"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("End-to-end encrypted authentication", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun OtpVerificationView(
    fullPhone: String,
    otpDigits: String,
    onOtpChange: (String) -> Unit,
    otpError: String?,
    resendCooldown: Int,
    attemptsRemaining: Int,
    isVerifying: Boolean,
    onResendClick: () -> Unit,
    onVerifyClick: () -> Unit,
    onChangeNumberClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.MarkEmailRead,
            contentDescription = null,
            tint = WhatsAppGreen,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter 6-Digit OTP",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "We sent an SMS with a 6-digit code to\n$fullPhone",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        TextButton(onClick = onChangeNumberClick) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Edit Phone Number", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // OTP Display Boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            for (i in 0 until 6) {
                val digit = otpDigits.getOrNull(i)?.toString() ?: ""
                val isFocused = otpDigits.length == i || (otpDigits.length == 6 && i == 5)
                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .border(
                            width = if (isFocused) 2.dp else 1.dp,
                            color = if (otpError != null) CoralRed else if (isFocused) WhatsAppGreen else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = digit,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Invisible / accessible input field for entering OTP digits
        OutlinedTextField(
            value = otpDigits,
            onValueChange = onOtpChange,
            placeholder = { Text("Type 6-digit OTP code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onVerifyClick() }),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .testTag("auth_otp_input")
        )

        if (otpError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = otpError,
                color = CoralRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resend Cooldown Timer
        if (resendCooldown > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Resend SMS in 0:${if (resendCooldown < 10) "0$resendCooldown" else "$resendCooldown"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            TextButton(onClick = onResendClick) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Resend Code via SMS", fontWeight = FontWeight.Bold, color = WhatsAppGreen)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onVerifyClick,
            enabled = otpDigits.length == 6 && !isVerifying,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("auth_verify_otp_button"),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
        ) {
            if (isVerifying) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Verify & Continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "$attemptsRemaining attempts remaining before temporary lockout",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileSetupView(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    statusText: String,
    onStatusChange: (String) -> Unit,
    emailAddress: String,
    onEmailChange: (String) -> Unit,
    avatarUrl: String,
    onChangePhotoClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Profile Info",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Please provide your name and an optional profile photo for your contacts.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Picture Picker
            Box(
                modifier = Modifier
                    .clickable { onChangePhotoClick() }
                    .testTag("auth_profile_avatar_box"),
                contentAlignment = Alignment.Center
            ) {
                UserAvatar(
                    name = fullName.ifBlank { "You" },
                    avatarUrl = avatarUrl,
                    size = 104.dp
                )

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(WhatsAppGreen)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Upload DP",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onChangePhotoClick) {
                Text(if (avatarUrl.isNotBlank()) "Change Photo" else "Add Profile Photo", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Full Name Input
            OutlinedTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                label = { Text("Full Name (Required)") },
                placeholder = { Text("e.g. Aman Verma") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_name_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Status/About Input
            OutlinedTextField(
                value = statusText,
                onValueChange = onStatusChange,
                label = { Text("About / Status") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_status_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Status Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Available", "At work 💻", "In a meeting", "Busy").forEach { chip ->
                    SuggestionChip(
                        onClick = { onStatusChange(chip) },
                        label = { Text(chip, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Optional Email for Account Recovery
            OutlinedTextField(
                value = emailAddress,
                onValueChange = onEmailChange,
                label = { Text("Email for Recovery (Optional)") },
                placeholder = { Text("you@example.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onFinishClick,
                enabled = fullName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("auth_finish_button"),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Chatting on ChatMitra", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun RecoverySuccessView(
    account: RegisteredAccount?,
    logoutOtherDevices: Boolean,
    onLogoutOtherDevicesChange: (Boolean) -> Unit,
    onContinueClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = WhatsAppGreen,
            modifier = Modifier.size(68.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Account Identity Verified!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Welcome back! Your ChatMitra account has been verified and restored.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Account Details Card
        account?.let { acc ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(name = acc.name, avatarUrl = acc.avatarUrl, size = 56.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(acc.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (acc.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(acc.phoneNumber, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Text(acc.statusText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📦 ${acc.backupMessagesCount} encrypted messages restored",
                            fontSize = 11.sp,
                            color = Color(0xFF00796B),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Lost device logout option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogoutOtherDevicesChange(!logoutOtherDevices) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = logoutOtherDevices,
                onCheckedChange = onLogoutOtherDevicesChange
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Log out from all other devices", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Recommended if your previous phone was lost, stolen, or replaced.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onContinueClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("auth_restore_continue_btn"),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
        ) {
            Text("Open ChatMitra", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
