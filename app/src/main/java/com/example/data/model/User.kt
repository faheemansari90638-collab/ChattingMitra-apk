package com.example.data.model

data class User(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val countryCode: String = "+91",
    val statusText: String = "Hey there! I am using ChatMitra.",
    val avatarUrl: String = "",
    val avatarColor: Long = 0xFF008069,
    val avatarPhotos: List<String> = listOf("avatar_1", "avatar_2"),
    val aboutBio: String = "Software engineer & privacy advocate. Building with ChatMitra 🌟",
    val customStatusEmoji: String = "🚀",
    val customStatusLabel: String = "Building",
    val emailAddress: String = "user@chatmitra.app",
    val profileHandle: String = "@chatmitra_user",
    val isOnline: Boolean = true,
    val lastSeen: String = "Online",
    val isBlocked: Boolean = false,
    val isVerified: Boolean = false,
    val verificationRequested: Boolean = false
)

data class Country(
    val name: String,
    val code: String,
    val flagEmoji: String,
    val isoCode: String = ""
)

val SampleCountries = listOf(
    Country("India", "+91", "🇮🇳", "IN"),
    Country("United States", "+1", "🇺🇸", "US"),
    Country("United Kingdom", "+44", "🇬🇧", "GB"),
    Country("United Arab Emirates", "+971", "🇦🇪", "AE"),
    Country("Saudi Arabia", "+966", "🇸🇦", "SA"),
    Country("Canada", "+1", "🇨🇦", "CA"),
    Country("Germany", "+49", "🇩🇪", "DE"),
    Country("Australia", "+61", "🇦🇺", "AU"),
    Country("Singapore", "+65", "🇸🇬", "SG"),
    Country("France", "+33", "🇫🇷", "FR"),
    Country("Pakistan", "+92", "🇵🇰", "PK"),
    Country("Bangladesh", "+880", "🇧🇩", "BD"),
    Country("Indonesia", "+62", "🇮🇩", "ID"),
    Country("Brazil", "+55", "🇧🇷", "BR"),
    Country("Nigeria", "+234", "🇳🇬", "NG"),
    Country("Egypt", "+20", "🇪🇬", "EG"),
    Country("Japan", "+81", "🇯🇵", "JP"),
    Country("South Africa", "+27", "🇿🇦", "ZA"),
    Country("Turkey", "+90", "🇹🇷", "TR"),
    Country("Italy", "+39", "🇮🇹", "IT"),
    Country("Spain", "+34", "🇪🇸", "ES"),
    Country("Mexico", "+52", "🇲🇽", "MX"),
    Country("Russia", "+7", "🇷🇺", "RU")
)
