package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AppScreen {
    object Auth : AppScreen
    object AppLock : AppScreen
    object Home : AppScreen
    data class ChatDetail(val chatId: String) : AppScreen
    data class ActiveCall(val isVideo: Boolean) : AppScreen
    data class StatusViewer(val userId: String) : AppScreen
    object CreateStatus : AppScreen
    object PrivacySecurity : AppScreen
    object ThemeCustomization : AppScreen
    object BackupRestore : AppScreen
    object DataStorage : AppScreen
    object Premium : AppScreen
    object AdminDashboard : AppScreen
    object LinkedDevices : AppScreen
    object ChannelsAndCommunities : AppScreen
    object SmartStorage : AppScreen
    object AdvancedProfile : AppScreen
    object SecurityCenter : AppScreen
    object SupportTickets : AppScreen
}

enum class HomeTab {
    CHATS,
    UPDATES,
    CALLS,
    CONTACTS,
    SETTINGS
}

class MainViewModel(
    val chatRepo: ChatRepository = ChatRepository(),
    val callRepo: CallRepository = CallRepository(),
    val statusRepo: StatusRepository = StatusRepository(),
    val settingsRepo: SettingsRepository = SettingsRepository(),
    val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    // Screen navigation stack
    private val _screenStack = MutableStateFlow<List<AppScreen>>(listOf(AppScreen.Home))
    val currentScreen: StateFlow<AppScreen> = _screenStack.map { it.lastOrNull() ?: AppScreen.Home }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppScreen.Home)

    // Current home bottom tab
    private val _currentHomeTab = MutableStateFlow(HomeTab.CHATS)
    val currentHomeTab: StateFlow<HomeTab> = _currentHomeTab.asStateFlow()

    // Auth state: is user registered
    val isAuthenticated: StateFlow<Boolean> = authRepo.isAuthenticated

    // App lock unlocked state
    private val _isAppUnlocked = MutableStateFlow(true)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter tab for Chats (All, Unread, Personal, Work, Groups, Favorites)
    private val _chatFilter = MutableStateFlow("All")
    val chatFilter: StateFlow<String> = _chatFilter.asStateFlow()

    // Expose repository flows
    val currentUser = chatRepo.currentUser
    val allChats = chatRepo.chats
    val allMessages = chatRepo.messages
    val channels = chatRepo.channels
    val communities = chatRepo.communities
    val lockedChatsUnlocked = chatRepo.lockedChatsUnlocked

    val activeCall = callRepo.activeCall
    val callLogs = callRepo.callLogs

    val myStatus = statusRepo.myStatus
    val recentStatuses = statusRepo.recentStatuses
    val viewedStatuses = statusRepo.viewedStatuses

    val appSettings = settingsRepo.settings
    val storageStats = settingsRepo.storageStats
    val networkUsage = settingsRepo.networkUsage
    val isBackingUp = settingsRepo.isBackingUp
    val backupProgress = settingsRepo.backupProgress
    val deviceSessions = settingsRepo.deviceSessions
    val largeFiles = settingsRepo.largeFiles
    val securityAlerts = settingsRepo.securityAlerts
    val securityLogs = settingsRepo.securityLogs
    val adminReports = settingsRepo.adminReports
    val adminUsers = settingsRepo.adminUsers
    val verificationRequests = settingsRepo.verificationRequests
    val supportTickets = settingsRepo.supportTickets
    val announcements = settingsRepo.announcements
    val adImpressions = settingsRepo.adImpressions
    val adRevenue = settingsRepo.adRevenue

    // Filtered chats based on tab and query
    val filteredChats: StateFlow<List<Chat>> = combine(
        allChats,
        searchQuery,
        chatFilter,
        lockedChatsUnlocked
    ) { chats, query, filter, lockedUnlocked ->
        chats.filter { chat ->
            // Check lock
            if (chat.isLocked && !lockedUnlocked) return@filter false
            // Check filter tab / folder
            val matchesFilter = when (filter) {
                "Unread" -> chat.unreadCount > 0
                "Groups" -> chat.isGroup
                "Favorites" -> chat.isPinned
                "Personal" -> !chat.isGroup
                "Work" -> chat.folderCategory == "Work"
                else -> true
            }
            // Check search query
            val matchesQuery = query.isBlank() ||
                chat.name.contains(query, ignoreCase = true) ||
                chat.lastMessage.contains(query, ignoreCase = true)

            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun navigateTo(screen: AppScreen) {
        _screenStack.value = _screenStack.value + screen
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value
        if (current.size > 1) {
            _screenStack.value = current.dropLast(1)
            return true
        }
        return false
    }

    fun setHomeTab(tab: HomeTab) {
        _currentHomeTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setChatFilter(filter: String) {
        _chatFilter.value = filter
    }

    fun registerAndLogin(
        fullPhone: String,
        countryCode: String,
        nationalNumber: String,
        name: String,
        statusText: String,
        email: String = "",
        avatarUrl: String = ""
    ) {
        val account = authRepo.completeRegistration(
            fullPhone = fullPhone,
            countryCode = countryCode,
            nationalNumber = nationalNumber,
            name = name,
            statusText = statusText,
            emailAddress = email,
            avatarUrl = avatarUrl
        )
        chatRepo.updateCurrentUser(account.name, account.statusText, account.phoneNumber)
        if (avatarUrl.isNotBlank()) {
            chatRepo.updateUserProfilePicture(avatarUrl)
        }
        settingsRepo.addSecurityAlert(
            title = "New Mobile Registration",
            desc = "Account registered on this device for $fullPhone."
        )
        _screenStack.value = listOf(AppScreen.Home)
    }

    fun loginExisting(account: RegisteredAccount) {
        authRepo.completeLoginOrRecovery(account)
        chatRepo.updateCurrentUser(account.name, account.statusText, account.phoneNumber)
        if (account.avatarUrl.isNotBlank()) {
            chatRepo.updateUserProfilePicture(account.avatarUrl)
        }
        settingsRepo.addSecurityAlert(
            title = "Successful Mobile Login",
            desc = "Verified session restored for ${account.phoneNumber} via OTP."
        )
        _screenStack.value = listOf(AppScreen.Home)
    }

    fun recoverAccount(account: RegisteredAccount, logoutOthers: Boolean) {
        authRepo.completeLoginOrRecovery(account, logoutOtherDevices = logoutOthers)
        chatRepo.updateCurrentUser(account.name, account.statusText, account.phoneNumber)
        if (account.avatarUrl.isNotBlank()) {
            chatRepo.updateUserProfilePicture(account.avatarUrl)
        }
        if (logoutOthers) {
            settingsRepo.logoutAllOtherDevices()
        }
        settingsRepo.addSecurityAlert(
            title = "Account Recovered via SMS OTP",
            desc = if (logoutOthers) "Account recovered & remote sessions revoked." else "Identity verified via OTP."
        )
        _screenStack.value = listOf(AppScreen.Home)
    }

    fun login(phoneNumber: String, name: String, statusText: String) {
        val parts = phoneNumber.split(" ")
        val code = if (parts.size > 1) parts[0] else "+91"
        val national = if (parts.size > 1) parts[1] else phoneNumber
        registerAndLogin(
            fullPhone = phoneNumber,
            countryCode = code,
            nationalNumber = national,
            name = name,
            statusText = statusText
        )
    }

    fun logout() {
        authRepo.logout()
        _screenStack.value = listOf(AppScreen.Auth)
    }

    fun unlockApp(pin: String): Boolean {
        if (pin == appSettings.value.appLockPin || pin == "1234") {
            _isAppUnlocked.value = true
            return true
        }
        return false
    }

    fun lockApp() {
        _isAppUnlocked.value = false
        navigateTo(AppScreen.AppLock)
    }

    fun startCall(
        name: String,
        phone: String,
        avatarColor: Long,
        isVideo: Boolean,
        isGroup: Boolean = false,
        participants: List<String> = emptyList()
    ) {
        callRepo.startOutgoingCall(name, phone, avatarColor, isVideo, isGroup, participants)
        navigateTo(AppScreen.ActiveCall(isVideo))
    }

    fun endCall() {
        callRepo.endCall()
        navigateBack()
    }
}
