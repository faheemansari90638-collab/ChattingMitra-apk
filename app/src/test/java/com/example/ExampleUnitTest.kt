package com.example

import com.example.data.model.MessageType
import com.example.data.repository.ChatRepository
import com.example.data.repository.SettingsRepository
import org.junit.Assert.*
import org.junit.Test

class ChatMitraUnitTest {

    @Test
    fun `test initial chats and user setup`() {
        val repo = ChatRepository()
        val user = repo.currentUser.value
        assertEquals("You", user.name)

        val chats = repo.chats.value
        assertTrue("Chats should be pre-loaded", chats.isNotEmpty())
        assertTrue("Aarav Sharma should be in chat list", chats.any { it.name == "Aarav Sharma" })
    }

    @Test
    fun `test sending message updates state`() {
        val repo = ChatRepository()
        val chatId = "chat_1"
        val initialCount = repo.messages.value[chatId]?.size ?: 0

        repo.sendMessage(
            chatId = chatId,
            text = "Testing ChatMitra messaging feature",
            type = MessageType.TEXT
        )

        val updatedList = repo.messages.value[chatId]
        assertNotNull(updatedList)
        assertEquals(initialCount + 1, updatedList!!.size)
        assertEquals("Testing ChatMitra messaging feature", updatedList.last().text)
    }

    @Test
    fun `test pin and lock chat toggles`() {
        val repo = ChatRepository()
        val chatId = "chat_1"
        val chat = repo.chats.value.first { it.id == chatId }
        val wasPinned = chat.isPinned

        repo.togglePinChat(chatId)
        val afterToggle = repo.chats.value.first { it.id == chatId }
        assertEquals(!wasPinned, afterToggle.isPinned)
    }

    @Test
    fun `test settings and app lock`() {
        val settingsRepo = SettingsRepository()
        assertEquals("1234", settingsRepo.settings.value.appLockPin)

        settingsRepo.setAppLock(true, "5678")
        assertTrue(settingsRepo.settings.value.isAppLockEnabled)
        assertEquals("5678", settingsRepo.settings.value.appLockPin)

        val clearedCache = settingsRepo.clearCache()
        assertTrue(clearedCache > 0f)
        assertEquals(0f, settingsRepo.storageStats.value.cacheMb, 0.01f)
    }

    @Test
    fun `test message reactions and mentions`() {
        val repo = ChatRepository()
        val chatId = "chat_1"
        val msg = repo.messages.value[chatId]!!.first()

        repo.addReaction(chatId, msg.id, "❤️")
        val updatedMsg = repo.messages.value[chatId]!!.first { it.id == msg.id }
        assertTrue(updatedMsg.reactions.containsKey("❤️"))
    }

    @Test
    fun `test interactive poll voting in group`() {
        val repo = ChatRepository()
        val chatId = "chat_2"
        val pollMsg = repo.messages.value[chatId]!!.first { it.type == MessageType.POLL }
        assertNotNull(pollMsg.pollData)
        val secondOption = pollMsg.pollData!!.options[1]

        repo.votePoll(chatId, pollMsg.id, secondOption.id, "You")
        val votedPollMsg = repo.messages.value[chatId]!!.first { it.id == pollMsg.id }
        assertTrue(votedPollMsg.pollData!!.options[1].voterNames.contains("You"))
    }

    @Test
    fun `test channels and communities`() {
        val repo = ChatRepository()
        val channels = repo.channels.value
        assertTrue("Channels should be loaded", channels.isNotEmpty())

        val ch1 = channels.first()
        val initialSubscribed = ch1.isSubscribed
        repo.toggleSubscribeChannel(ch1.id)
        val updatedCh = repo.channels.value.first { it.id == ch1.id }
        assertEquals(!initialSubscribed, updatedCh.isSubscribed)

        val communities = repo.communities.value
        assertTrue("Communities should be loaded", communities.isNotEmpty())
    }

    @Test
    fun `test multi-device sessions and verification approval`() {
        val settingsRepo = SettingsRepository()
        val initialSessions = settingsRepo.deviceSessions.value.size

        settingsRepo.linkNewDevice("MacBook Air M3", "macOS Sonoma • Safari")
        assertEquals(initialSessions + 1, settingsRepo.deviceSessions.value.size)

        // Verification approval
        assertTrue(settingsRepo.verificationRequests.value.contains("Aarav Sharma"))
        settingsRepo.approveVerification("Aarav Sharma")
        assertFalse(settingsRepo.verificationRequests.value.contains("Aarav Sharma"))

        val aarav = settingsRepo.adminUsers.value.first { it.name == "Aarav Sharma" }
        assertTrue(aarav.isVerified)

        // Support ticket resolution
        val openTicket = settingsRepo.supportTickets.value.first { it.id == "t_101" }
        assertEquals("Open", openTicket.status)
        settingsRepo.replySupportTicket("t_101", "Wi-Fi speeds restored.")
        val resolvedTicket = settingsRepo.supportTickets.value.first { it.id == "t_101" }
        assertEquals("Resolved", resolvedTicket.status)
        assertEquals("Wi-Fi speeds restored.", resolvedTicket.reply)
    }

    @Test
    fun `test phone number validation`() {
        val authRepo = com.example.data.repository.AuthRepository()
        assertNotNull(authRepo.validatePhoneNumber("12345")) // too short
        assertNotNull(authRepo.validatePhoneNumber("1234567890123456")) // too long
        assertNull(authRepo.validatePhoneNumber("9876543210")) // valid
    }

    @Test
    fun `test duplicate registration is prevented`() {
        val authRepo = com.example.data.repository.AuthRepository()
        // "+91 98111 22334" is already registered (Aarav Sharma)
        val result = authRepo.sendOtp("+91", "9811122334", "REGISTER")
        assertTrue(result is com.example.data.repository.SendOtpResult.Error)
        val errorMsg = (result as com.example.data.repository.SendOtpResult.Error).message
        assertTrue(errorMsg.contains("already registered"))
    }

    @Test
    fun `test full registration and otp verification flow`() {
        val authRepo = com.example.data.repository.AuthRepository()
        val sendResult = authRepo.sendOtp("+91", "9988776655", "REGISTER")
        assertTrue("OTP should send successfully", sendResult is com.example.data.repository.SendOtpResult.Success)
        val success = sendResult as com.example.data.repository.SendOtpResult.Success
        val verificationId = success.verificationId
        val validCode = success.autoFillCode

        // Wrong code attempt
        val failedVerify = authRepo.verifyOtp(verificationId, "000000")
        assertTrue(failedVerify is com.example.data.repository.VerifyOtpResult.Error)
        val err = failedVerify as com.example.data.repository.VerifyOtpResult.Error
        assertEquals(4, err.attemptsLeft)

        // Correct code verification
        val verifyResult = authRepo.verifyOtp(verificationId, validCode)
        assertTrue(verifyResult is com.example.data.repository.VerifyOtpResult.Success)
        val verifySuccess = verifyResult as com.example.data.repository.VerifyOtpResult.Success
        assertTrue(verifySuccess.isNewUser)

        // Complete registration
        authRepo.completeRegistration(
            fullPhone = "+91 99887 76655",
            countryCode = "+91",
            nationalNumber = "9988776655",
            name = "Rohan Das",
            statusText = "Exploring ChatMitra",
            emailAddress = "rohan@example.com",
            avatarUrl = "file:///data/user/0/com.example/files/avatar_test.jpg"
        )

        assertTrue(authRepo.isPhoneNumberRegistered("+91 99887 76655"))
        val account = authRepo.getAccount("+91 99887 76655")
        assertNotNull(account)
        assertEquals("Rohan Das", account?.name)
        assertEquals("Exploring ChatMitra", account?.statusText)
    }

    @Test
    fun `test existing user login and account recovery`() {
        val authRepo = com.example.data.repository.AuthRepository()
        // Login existing user
        val sendLogin = authRepo.sendOtp("+91", "9811122334", "LOGIN")
        assertTrue(sendLogin is com.example.data.repository.SendOtpResult.Success)
        val loginSuccess = sendLogin as com.example.data.repository.SendOtpResult.Success
        val verifyLogin = authRepo.verifyOtp(loginSuccess.verificationId, loginSuccess.autoFillCode)
        assertTrue(verifyLogin is com.example.data.repository.VerifyOtpResult.Success)
        val loginResult = verifyLogin as com.example.data.repository.VerifyOtpResult.Success
        assertFalse(loginResult.isNewUser)
        assertEquals("Aarav Sharma", loginResult.account?.name)
    }

    @Test
    fun `test profile picture and wallpaper updates in chat repository`() {
        val chatRepo = ChatRepository()
        val testAvatar = "file:///data/media/avatar_custom.png"
        chatRepo.updateUserProfilePicture(testAvatar)
        assertEquals(testAvatar, chatRepo.currentUser.value.avatarUrl)

        // Test custom chat wallpaper
        val testWallpaper = "file:///data/media/wallpaper_sunset.jpg"
        chatRepo.setChatCustomWallpaper("chat_1", testWallpaper)
        val chat1 = chatRepo.chats.value.first { it.id == "chat_1" }
        assertEquals(testWallpaper, chat1.customWallpaperUri)

        // Remove DP
        chatRepo.removeUserProfilePicture()
        assertEquals("", chatRepo.currentUser.value.avatarUrl)
    }
}
