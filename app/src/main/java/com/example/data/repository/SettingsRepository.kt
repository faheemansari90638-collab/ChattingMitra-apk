package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class AdminReport(
    val id: String,
    val reportedUser: String,
    val reportedPhone: String,
    val reason: String,
    val timestamp: String,
    val status: String = "PENDING"
)

data class AdminUserRecord(
    val id: String,
    val name: String,
    val phone: String,
    val joinedDate: String,
    val isSuspended: Boolean = false,
    val isPremium: Boolean = false,
    val isVerified: Boolean = false
)

data class StorageStats(
    val photosMb: Float = 142.4f,
    val videosMb: Float = 318.2f,
    val documentsMb: Float = 48.6f,
    val voiceNotesMb: Float = 26.5f,
    val cacheMb: Float = 85.0f,
    val totalDeviceFreeGb: Float = 48.2f
)

data class NetworkUsage(
    val messagesSentMb: Float = 14.2f,
    val messagesReceivedMb: Float = 28.5f,
    val mediaSentMb: Float = 120.4f,
    val mediaReceivedMb: Float = 245.8f,
    val callsSentMb: Float = 85.3f,
    val callsReceivedMb: Float = 94.6f,
    val statusSentMb: Float = 18.0f,
    val statusReceivedMb: Float = 62.1f
)

class SettingsRepository {
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _storageStats = MutableStateFlow(StorageStats())
    val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

    private val _networkUsage = MutableStateFlow(NetworkUsage())
    val networkUsage: StateFlow<NetworkUsage> = _networkUsage.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    private val _backupProgress = MutableStateFlow(0f)
    val backupProgress: StateFlow<Float> = _backupProgress.asStateFlow()

    // Multi-Device Sessions
    private val _deviceSessions = MutableStateFlow<List<DeviceSession>>(emptyList())
    val deviceSessions: StateFlow<List<DeviceSession>> = _deviceSessions.asStateFlow()

    // Security Alerts & Logs
    private val _securityAlerts = MutableStateFlow<List<SecurityAlert>>(emptyList())
    val securityAlerts: StateFlow<List<SecurityAlert>> = _securityAlerts.asStateFlow()

    private val _securityLogs = MutableStateFlow<List<String>>(emptyList())
    val securityLogs: StateFlow<List<String>> = _securityLogs.asStateFlow()

    // Smart Storage: Large Files & Duplicates
    private val _largeFiles = MutableStateFlow<List<LargeMediaFile>>(emptyList())
    val largeFiles: StateFlow<List<LargeMediaFile>> = _largeFiles.asStateFlow()

    // Admin Dashboard state
    private val _adminReports = MutableStateFlow<List<AdminReport>>(emptyList())
    val adminReports: StateFlow<List<AdminReport>> = _adminReports.asStateFlow()

    private val _adminUsers = MutableStateFlow<List<AdminUserRecord>>(emptyList())
    val adminUsers: StateFlow<List<AdminUserRecord>> = _adminUsers.asStateFlow()

    private val _verificationRequests = MutableStateFlow<List<String>>(
        listOf("Aarav Sharma", "Sneha Kulkarni", "Devendra Joshi")
    )
    val verificationRequests: StateFlow<List<String>> = _verificationRequests.asStateFlow()

    private val _supportTickets = MutableStateFlow<List<SupportTicket>>(emptyList())
    val supportTickets: StateFlow<List<SupportTicket>> = _supportTickets.asStateFlow()

    private val _announcements = MutableStateFlow<List<String>>(
        listOf("ChatMitra v2.0 is live! Enjoy high quality calling & custom wallpapers.")
    )
    val announcements: StateFlow<List<String>> = _announcements.asStateFlow()

    val adImpressions = MutableStateFlow(48920)
    val adRevenue = MutableStateFlow(348.50f)

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // Multi-device sessions
        _deviceSessions.value = listOf(
            DeviceSession(
                id = "dev_curr",
                deviceName = "Google Pixel 9 Pro (This phone)",
                deviceType = "Android 16 • ChatMitra App",
                location = "Bangalore, India",
                ipAddress = "49.207.214.18",
                lastActive = "Active now",
                isCurrent = true
            ),
            DeviceSession(
                id = "dev_web",
                deviceName = "ChatMitra Web • Chrome",
                deviceType = "macOS Sequoia",
                location = "Bangalore, India",
                ipAddress = "49.207.214.22",
                lastActive = "Today at 3:15 PM"
            ),
            DeviceSession(
                id = "dev_win",
                deviceName = "ChatMitra Desktop",
                deviceType = "Windows 11 Pro",
                location = "Mumbai, India",
                ipAddress = "103.21.144.9",
                lastActive = "Yesterday at 7:40 PM"
            )
        )

        // Security Alerts
        _securityAlerts.value = listOf(
            SecurityAlert(
                id = "sec_1",
                title = "New Login from macOS Chrome",
                description = "Recognized device logged into ChatMitra Web via QR Code sync.",
                timestamp = "Today at 3:15 PM",
                isCritical = false
            )
        )

        _securityLogs.value = listOf(
            "Nov 7, 18:10: Successful biometric authentication",
            "Nov 7, 15:15: Web pairing session initialized via secure QR code",
            "Nov 6, 09:20: End-to-end 64-bit encryption keys validated"
        )

        // Large Files
        _largeFiles.value = listOf(
            LargeMediaFile("lf_1", "Sprint_Demo_Recording_Full.mp4", "Video", 88.5f, "Tech Innovators Group 💻", "Nov 5"),
            LargeMediaFile("lf_2", "Annual_Finance_Audit_2026.pdf", "Document", 24.2f, "Priya Patel", "Nov 4"),
            LargeMediaFile("lf_3", "Family_Vacation_Highlights.mov", "Video", 145.0f, "Family Hub ❤️", "Nov 2"),
            LargeMediaFile("lf_4", "HighRes_Event_Posters.zip", "Document", 32.8f, "Aarav Sharma", "Oct 29")
        )

        // Admin Reports
        _adminReports.value = listOf(
            AdminReport(
                id = "rep_1",
                reportedUser = "Spam Bot 99",
                reportedPhone = "+1 800 555 0199",
                reason = "Sending unsolicited commercial messages and promotional links",
                timestamp = "Today, 10:15 AM"
            ),
            AdminReport(
                id = "rep_2",
                reportedUser = "Unknown Caller",
                reportedPhone = "+91 91234 56789",
                reason = "Impersonation and abusive behavior",
                timestamp = "Yesterday"
            )
        )

        _adminUsers.value = listOf(
            AdminUserRecord("u_1", "Aarav Sharma", "+91 98111 22334", "Oct 12, 2026", isPremium = true, isVerified = true),
            AdminUserRecord("u_2", "Priya Patel", "+91 98222 33445", "Oct 15, 2026", isPremium = false, isVerified = true),
            AdminUserRecord("u_3", "Rohan Verma", "+91 98333 44556", "Oct 18, 2026", isPremium = true, isVerified = false),
            AdminUserRecord("u_4", "Sneha Kulkarni", "+91 98444 55667", "Oct 24, 2026", isPremium = false, isVerified = false),
            AdminUserRecord("u_5", "Suspicious User", "+1 800 555 0199", "Nov 01, 2026", isSuspended = true)
        )

        // Support tickets
        _supportTickets.value = listOf(
            SupportTicket(
                id = "t_101",
                subject = "Media backup restoring speed",
                userEmail = "user1@example.com",
                status = "Open",
                message = "How can I restore my 4GB file backups faster over Wi-Fi?",
                date = "Today, 11:00 AM"
            ),
            SupportTicket(
                id = "t_102",
                subject = "Premium subscription receipt",
                userEmail = "rohan@example.com",
                status = "Resolved",
                message = "Can I get an invoice copy for my annual subscription?",
                date = "Yesterday",
                reply = "Invoice sent to your registered email address."
            )
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun setWallpaper(wallpaper: ChatWallpaper) {
        _settings.value = _settings.value.copy(wallpaper = wallpaper, customWallpaperUri = null)
    }

    fun setCustomWallpaperUri(uriString: String?) {
        _settings.value = _settings.value.copy(customWallpaperUri = uriString)
    }

    fun setBubbleStyle(style: BubbleStyle) {
        _settings.value = _settings.value.copy(bubbleStyle = style)
    }

    fun setFontSize(size: FontSize) {
        _settings.value = _settings.value.copy(fontSize = size)
    }

    fun setAppLock(enabled: Boolean, pin: String) {
        _settings.value = _settings.value.copy(
            isAppLockEnabled = enabled,
            appLockPin = pin
        )
    }

    fun setLanguage(lang: String) {
        _settings.value = _settings.value.copy(selectedLanguage = lang)
    }

    fun setPrivacy(lastSeen: String, profilePhoto: String, status: String) {
        _settings.value = _settings.value.copy(
            lastSeenPrivacy = lastSeen,
            profilePhotoPrivacy = profilePhoto,
            statusPrivacy = status
        )
    }

    fun setLowDataMode(enabled: Boolean) {
        _settings.value = _settings.value.copy(lowDataMode = enabled)
    }

    fun setBatterySaver(enabled: Boolean) {
        _settings.value = _settings.value.copy(batterySaverMode = enabled)
    }

    fun setHighContrast(enabled: Boolean) {
        _settings.value = _settings.value.copy(highContrastMode = enabled)
    }

    fun setCompactHomeLayout(enabled: Boolean) {
        _settings.value = _settings.value.copy(compactHomeLayout = enabled)
    }

    fun addCustomFolder(folderName: String) {
        if (!_settings.value.customFolders.contains(folderName)) {
            _settings.value = _settings.value.copy(
                customFolders = _settings.value.customFolders + folderName
            )
        }
    }

    fun linkNewDevice(deviceName: String, deviceType: String) {
        val newSession = DeviceSession(
            id = "dev_${UUID.randomUUID().toString().take(6)}",
            deviceName = deviceName,
            deviceType = deviceType,
            location = "Bangalore, India",
            ipAddress = "49.207.214.${(10..99).random()}",
            lastActive = "Active just now"
        )
        _deviceSessions.value = _deviceSessions.value + newSession
    }

    fun removeDeviceSession(sessionId: String) {
        _deviceSessions.value = _deviceSessions.value.filterNot { it.id == sessionId }
    }

    fun logoutAllOtherDevices() {
        _deviceSessions.value = _deviceSessions.value.filter { it.isCurrent }
    }

    fun addSecurityAlert(title: String, desc: String, isCritical: Boolean = false) {
        val newAlert = SecurityAlert(
            id = "sec_${UUID.randomUUID().toString().take(6)}",
            title = title,
            description = desc,
            timestamp = "Just now",
            isCritical = isCritical
        )
        _securityAlerts.value = listOf(newAlert) + _securityAlerts.value
        val timeStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date())
        _securityLogs.value = listOf("$timeStr: $title") + _securityLogs.value
    }

    fun deleteLargeFile(fileId: String) {
        _largeFiles.value = _largeFiles.value.filterNot { it.id == fileId }
    }

    fun deleteDuplicateDownloads(): Float {
        val freedMb = 48.5f
        _storageStats.value = _storageStats.value.copy(
            cacheMb = (_storageStats.value.cacheMb - 20f).coerceAtLeast(0f)
        )
        return freedMb
    }

    fun setNotifications(sound: String, vibrate: Boolean) {
        _settings.value = _settings.value.copy(
            notificationSound = sound,
            vibrateEnabled = vibrate
        )
    }

    fun activatePremium() {
        _settings.value = _settings.value.copy(
            isPremium = true,
            showBannerAd = false
        )
    }

    suspend fun performBackup() {
        _isBackingUp.value = true
        _backupProgress.value = 0f
        for (i in 1..10) {
            delay(120)
            _backupProgress.value = i / 10f
        }
        val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date())
        _settings.value = _settings.value.copy(lastBackupDate = dateStr)
        _isBackingUp.value = false
    }

    fun clearCache(): Float {
        val cleared = _storageStats.value.cacheMb
        _storageStats.value = _storageStats.value.copy(cacheMb = 0f)
        return cleared
    }

    // Admin & Business Tools
    fun approveVerification(userName: String) {
        _verificationRequests.value = _verificationRequests.value.filterNot { it == userName }
        _adminUsers.value = _adminUsers.value.map {
            if (it.name == userName) it.copy(isVerified = true) else it
        }
    }

    fun rejectVerification(userName: String) {
        _verificationRequests.value = _verificationRequests.value.filterNot { it == userName }
    }

    fun replySupportTicket(ticketId: String, replyText: String) {
        _supportTickets.value = _supportTickets.value.map {
            if (it.id == ticketId) it.copy(status = "Resolved", reply = replyText) else it
        }
    }

    fun dismissReport(reportId: String) {
        _adminReports.value = _adminReports.value.filterNot { it.id == reportId }
    }

    fun suspendUser(userId: String) {
        _adminUsers.value = _adminUsers.value.map {
            if (it.id == userId) it.copy(isSuspended = true) else it
        }
    }

    fun activateUser(userId: String) {
        _adminUsers.value = _adminUsers.value.map {
            if (it.id == userId) it.copy(isSuspended = false) else it
        }
    }

    fun postAnnouncement(text: String) {
        _announcements.value = listOf(text) + _announcements.value
    }
}
