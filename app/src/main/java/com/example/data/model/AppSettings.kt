package com.example.data.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class ChatWallpaper(val displayName: String, val colorHex: Long) {
    CLASSIC_DOODLE("WhatsApp Classic", 0xFFEFEAE2),
    EMERALD_MINT("Mint Emerald", 0xFFE0F2F1),
    DARK_SLATE("Dark Slate", 0xFF0B141A),
    PURPLE_DUSK("Purple Dusk", 0xFFEDE7F6),
    SUNSET_WARM("Warm Peach", 0xFFFBE9E7)
}

enum class BubbleStyle(val displayName: String) {
    ROUNDED_MODERN("3D Rounded Modern"),
    WHATSAPP_CLASSIC("WhatsApp Classic"),
    PILL_SLEEK("Pill Sleek")
}

enum class FontSize(val scale: Float, val label: String) {
    SMALL(0.88f, "Small"),
    MEDIUM(1.0f, "Medium"),
    LARGE(1.15f, "Large")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val wallpaper: ChatWallpaper = ChatWallpaper.CLASSIC_DOODLE,
    val bubbleStyle: BubbleStyle = BubbleStyle.ROUNDED_MODERN,
    val fontSize: FontSize = FontSize.MEDIUM,
    val isAppLockEnabled: Boolean = false,
    val appLockPin: String = "1234",
    val autoLockTimeMinutes: Int = 1,
    val lastSeenPrivacy: String = "Everyone",
    val profilePhotoPrivacy: String = "Everyone",
    val statusPrivacy: String = "My Contacts",
    val readReceipts: Boolean = true,
    val defaultDisappearingHours: Int = 0,
    val selectedLanguage: String = "English",
    val isPremium: Boolean = false,
    val showBannerAd: Boolean = true,
    val backupFrequency: String = "Daily",
    val lastBackupDate: String = "Today at 02:00 AM",
    val backupSizeMb: Float = 42.5f,
    val mediaAutoDownloadWifi: Boolean = true,
    val mediaAutoDownloadCellular: Boolean = false,
    val notificationSound: String = "ChatMitra Chime",
    val vibrateEnabled: Boolean = true,
    // UX & Accessibility
    val lowDataMode: Boolean = false,
    val batterySaverMode: Boolean = false,
    val highContrastMode: Boolean = false,
    val compactHomeLayout: Boolean = false,
    val customFolders: List<String> = listOf("All", "Unread", "Personal", "Work", "Groups"),
    val e2eBackupKey: String = "7F9A-8C4B-2D1E-990A-5F3C-1B7E-4D2A",
    val autoMediaCleanupDays: Int = 30,
    val customWallpaperUri: String? = null
)
