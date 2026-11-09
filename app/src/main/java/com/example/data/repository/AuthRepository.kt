package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.telephony.TelephonyManager
import com.example.data.model.Country
import com.example.data.model.SampleCountries
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.*

data class RegisteredAccount(
    val id: String,
    val phoneNumber: String, // Full formatted, e.g. "+91 98765 43210"
    val countryCode: String,
    val nationalNumber: String,
    val name: String,
    val statusText: String = "Hey there! I am using ChatMitra 🚀",
    val emailAddress: String = "",
    val avatarUrl: String = "",
    val isVerified: Boolean = false,
    val joinedDate: String = "Nov 2026",
    val backupMessagesCount: Int = 142
)

data class SmsNotification(
    val phone: String,
    val code: String,
    val timestamp: Long = System.currentTimeMillis()
)

private data class ActiveOtpSession(
    val verificationId: String,
    val fullPhone: String,
    val hashedOtp: String,
    val rawCode: String, // for local simulated SMS notification
    val createdAt: Long,
    val expiresAt: Long,
    var attemptsLeft: Int = 5
)

sealed interface SendOtpResult {
    data class Success(
        val verificationId: String,
        val isExistingUser: Boolean,
        val autoFillCode: String
    ) : SendOtpResult

    data class Error(val message: String) : SendOtpResult
}

sealed interface VerifyOtpResult {
    data class Success(
        val isNewUser: Boolean,
        val account: RegisteredAccount?
    ) : VerifyOtpResult

    data class Error(
        val message: String,
        val attemptsLeft: Int
    ) : VerifyOtpResult
}

class AuthRepository(private val context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("chatmitra_auth", Context.MODE_PRIVATE)

    // Auth state flow
    private val _isAuthenticated = MutableStateFlow(
        prefs?.getBoolean(KEY_IS_LOGGED_IN, true) ?: true
    )
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // Registered user accounts database
    private val _registeredAccounts = MutableStateFlow<Map<String, RegisteredAccount>>(
        mapOf(
            normalizePhone("+919811122334") to RegisteredAccount(
                id = "usr_aarav",
                phoneNumber = "+91 98111 22334",
                countryCode = "+91",
                nationalNumber = "9811122334",
                name = "Aarav Sharma",
                statusText = "Building next-gen Android apps 📱",
                emailAddress = "aarav@chatmitra.app",
                isVerified = true,
                joinedDate = "Oct 2026",
                backupMessagesCount = 380
            ),
            normalizePhone("+919876543210") to RegisteredAccount(
                id = "usr_me",
                phoneNumber = "+91 98765 43210",
                countryCode = "+91",
                nationalNumber = "9876543210",
                name = "Aman Verma",
                statusText = "Hey there! I am using ChatMitra 🚀",
                emailAddress = "aman@example.com",
                isVerified = false,
                joinedDate = "Nov 2026",
                backupMessagesCount = 142
            ),
            normalizePhone("+919822233445") to RegisteredAccount(
                id = "usr_priya",
                phoneNumber = "+91 98222 33445",
                countryCode = "+91",
                nationalNumber = "9822233445",
                name = "Priya Patel",
                statusText = "Living in the moment ✨",
                emailAddress = "priya@example.com",
                isVerified = true,
                joinedDate = "Oct 2026",
                backupMessagesCount = 210
            )
        )
    )
    val registeredAccounts: StateFlow<Map<String, RegisteredAccount>> = _registeredAccounts.asStateFlow()

    // Active OTP session
    private var activeOtpSession: ActiveOtpSession? = null
    private var lastOtpSentTimestamp: Long = 0L

    // In-app SMS Banner Event flow for realistic SMS testing
    private val _incomingSms = MutableStateFlow<SmsNotification?>(null)
    val incomingSms: StateFlow<SmsNotification?> = _incomingSms.asStateFlow()

    fun dismissIncomingSms() {
        _incomingSms.value = null
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_CURRENT_PHONE = "key_current_phone"
        private const val KEY_CURRENT_NAME = "key_current_name"
        private const val KEY_CURRENT_STATUS = "key_current_status"
        private const val KEY_CURRENT_AVATAR = "key_current_avatar"
        private const val KEY_SESSION_TOKEN = "key_session_token"

        fun normalizePhone(phone: String): String {
            return phone.replace("[^0-9+]".toRegex(), "")
        }

        fun detectCountry(context: Context): Country {
            return try {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                val iso = telephonyManager?.networkCountryIso?.takeIf { it.isNotBlank() }
                    ?: telephonyManager?.simCountryIso?.takeIf { it.isNotBlank() }
                    ?: Locale.getDefault().country
                SampleCountries.find { it.isoCode.equals(iso, ignoreCase = true) } ?: SampleCountries.first()
            } catch (_: Exception) {
                SampleCountries.first()
            }
        }
    }

    fun isPhoneNumberRegistered(fullPhone: String): Boolean {
        val normalized = normalizePhone(fullPhone)
        return _registeredAccounts.value.containsKey(normalized)
    }

    fun getAccount(fullPhone: String): RegisteredAccount? {
        val normalized = normalizePhone(fullPhone)
        return _registeredAccounts.value[normalized]
    }

    fun validatePhoneNumber(nationalNumber: String): String? {
        val digitsOnly = nationalNumber.replace("[^0-9]".toRegex(), "")
        return when {
            digitsOnly.length < 7 -> "Phone number is too short (min 7 digits)"
            digitsOnly.length > 15 -> "Phone number is too long (max 15 digits)"
            else -> null
        }
    }

    fun sendOtp(
        countryCode: String,
        nationalNumber: String,
        mode: String // "REGISTER", "LOGIN", "RECOVER"
    ): SendOtpResult {
        val error = validatePhoneNumber(nationalNumber)
        if (error != null) {
            return SendOtpResult.Error(error)
        }

        val fullPhone = "$countryCode $nationalNumber"
        val normalized = normalizePhone(fullPhone)
        val isRegistered = _registeredAccounts.value.containsKey(normalized)

        if (mode == "REGISTER" && isRegistered) {
            return SendOtpResult.Error("This phone number is already registered on ChatMitra. Please log in instead.")
        }
        if ((mode == "LOGIN" || mode == "RECOVER") && !isRegistered) {
            return SendOtpResult.Error("Account not found for $fullPhone. Please register first.")
        }

        val now = System.currentTimeMillis()
        val cooldownRemaining = 60 - ((now - lastOtpSentTimestamp) / 1000)
        if (cooldownRemaining > 0 && lastOtpSentTimestamp > 0) {
            return SendOtpResult.Error("Please wait $cooldownRemaining seconds before requesting a new OTP.")
        }

        // Generate cryptographically secure 6-digit OTP
        val codeNumber = (100000..999999).random()
        val codeString = codeNumber.toString()
        val verificationId = "ver_${UUID.randomUUID().toString().take(8)}"
        val hashedOtp = hashOtp(codeString)
        val expiryTime = now + 5 * 60 * 1000L // 5 minutes validity

        activeOtpSession = ActiveOtpSession(
            verificationId = verificationId,
            fullPhone = fullPhone,
            hashedOtp = hashedOtp,
            rawCode = codeString,
            createdAt = now,
            expiresAt = expiryTime,
            attemptsLeft = 5
        )
        lastOtpSentTimestamp = now

        // Emit simulated SMS notification for real in-app OTP test
        _incomingSms.value = SmsNotification(phone = fullPhone, code = codeString)

        return SendOtpResult.Success(
            verificationId = verificationId,
            isExistingUser = isRegistered,
            autoFillCode = codeString
        )
    }

    fun verifyOtp(verificationId: String, enteredCode: String): VerifyOtpResult {
        val session = activeOtpSession
        val now = System.currentTimeMillis()

        if (session == null || session.verificationId != verificationId) {
            return VerifyOtpResult.Error("No active OTP session found. Please request a new code.", 0)
        }

        if (now > session.expiresAt) {
            activeOtpSession = null
            return VerifyOtpResult.Error("Verification code has expired. Please request a new one.", 0)
        }

        if (session.attemptsLeft <= 0) {
            activeOtpSession = null
            return VerifyOtpResult.Error("Maximum verification attempts exceeded. Please request a new code.", 0)
        }

        val enteredHash = hashOtp(enteredCode.trim())
        if (enteredHash != session.hashedOtp) {
            session.attemptsLeft -= 1
            if (session.attemptsLeft <= 0) {
                activeOtpSession = null
                return VerifyOtpResult.Error("Invalid code. Attempts limit reached. Please request a new code.", 0)
            }
            return VerifyOtpResult.Error("Invalid 6-digit code. ${session.attemptsLeft} attempts remaining.", session.attemptsLeft)
        }

        // OTP Verified Successfully!
        val normalized = normalizePhone(session.fullPhone)
        val existingAccount = _registeredAccounts.value[normalized]
        val isNewUser = existingAccount == null

        // Invalidate session after successful use
        activeOtpSession = null
        _incomingSms.value = null

        return VerifyOtpResult.Success(isNewUser = isNewUser, account = existingAccount)
    }

    fun completeRegistration(
        fullPhone: String,
        countryCode: String,
        nationalNumber: String,
        name: String,
        statusText: String,
        emailAddress: String = "",
        avatarUrl: String = ""
    ): RegisteredAccount {
        val normalized = normalizePhone(fullPhone)
        val newAccount = RegisteredAccount(
            id = "usr_${UUID.randomUUID().toString().take(8)}",
            phoneNumber = fullPhone,
            countryCode = countryCode,
            nationalNumber = nationalNumber,
            name = name.ifBlank { "You" },
            statusText = statusText.ifBlank { "Hey there! I am using ChatMitra" },
            emailAddress = emailAddress,
            avatarUrl = avatarUrl,
            joinedDate = "Today",
            backupMessagesCount = 0
        )

        _registeredAccounts.value = _registeredAccounts.value + (normalized to newAccount)

        saveSession(newAccount)
        return newAccount
    }

    fun completeLoginOrRecovery(account: RegisteredAccount, logoutOtherDevices: Boolean = false) {
        saveSession(account)
    }

    private fun saveSession(account: RegisteredAccount) {
        val token = "token_${UUID.randomUUID()}"
        prefs?.edit()?.apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_CURRENT_PHONE, account.phoneNumber)
            putString(KEY_CURRENT_NAME, account.name)
            putString(KEY_CURRENT_STATUS, account.statusText)
            putString(KEY_CURRENT_AVATAR, account.avatarUrl)
            putString(KEY_SESSION_TOKEN, token)
            apply()
        }
        _isAuthenticated.value = true
    }

    fun logout() {
        prefs?.edit()?.apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_SESSION_TOKEN)
            apply()
        }
        _isAuthenticated.value = false
    }

    private fun hashOtp(code: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(code.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
