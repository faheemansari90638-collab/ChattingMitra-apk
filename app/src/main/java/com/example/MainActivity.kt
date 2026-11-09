package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.model.ThemeMode
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.call.CallScreen
import com.example.ui.screens.chat.ChatDetailScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.lock.AppLockScreen
import com.example.ui.screens.channels.ChannelsAndCommunitiesScreen
import com.example.ui.screens.devices.LinkedDevicesScreen
import com.example.ui.screens.profile.AdvancedProfileScreen
import com.example.ui.screens.security.SecurityCenterScreen
import com.example.ui.screens.settings.*
import com.example.ui.screens.status.CreateStatusScreen
import com.example.ui.screens.status.StatusViewerScreen
import com.example.ui.screens.storage.SmartStorageScreen
import com.example.ui.theme.ChatMitraTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.appSettings.collectAsState()
            val isAuthenticated by viewModel.isAuthenticated.collectAsState()
            val isAppUnlocked by viewModel.isAppUnlocked.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            val isDark = when (settings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            ChatMitraTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when {
                        !isAuthenticated -> {
                            AuthScreen(viewModel = viewModel)
                        }
                        settings.isAppLockEnabled && !isAppUnlocked -> {
                            AppLockScreen(viewModel = viewModel)
                        }
                        else -> {
                            ChatMitraAppNavigation(
                                currentScreen = currentScreen,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMitraAppNavigation(
    currentScreen: AppScreen,
    viewModel: MainViewModel
) {
    when (currentScreen) {
        is AppScreen.Home -> {
            HomeScreen(viewModel = viewModel)
        }
        is AppScreen.ChatDetail -> {
            ChatDetailScreen(
                chatId = currentScreen.chatId,
                viewModel = viewModel,
                onBackClick = { viewModel.navigateBack() }
            )
        }
        is AppScreen.ActiveCall -> {
            CallScreen(
                isVideo = currentScreen.isVideo,
                viewModel = viewModel,
                onEndCallClick = { viewModel.endCall() }
            )
        }
        is AppScreen.StatusViewer -> {
            StatusViewerScreen(
                userId = currentScreen.userId,
                viewModel = viewModel,
                onCloseClick = { viewModel.navigateBack() }
            )
        }
        is AppScreen.CreateStatus -> {
            CreateStatusScreen(
                viewModel = viewModel,
                onBackClick = { viewModel.navigateBack() }
            )
        }
        is AppScreen.PrivacySecurity -> {
            PrivacySecurityScreen(
                viewModel = viewModel,
                onBackClick = { viewModel.navigateBack() }
            )
        }
        is AppScreen.ThemeCustomization -> {
            ThemeCustomizationScreen(
                viewModel = viewModel,
                onBackClick = { viewModel.navigateBack() }
            )
        }
        is AppScreen.BackupRestore -> {
            BackupRestoreScreen(
                viewModel = viewModel,
                onBackClick = { viewModel.navigateBack() }
            )
        }
        is AppScreen.DataStorage -> {
            DataStorageScreen(
                viewModel = viewModel,
                onBackClick = { viewModel.navigateBack() }
            )
        }
        is AppScreen.Premium -> {
            PremiumScreen(
                viewModel = viewModel,
                onBackClick = { viewModel.navigateBack() }
            )
        }
        is AppScreen.AdminDashboard -> {
            AdminDashboardScreen(
                viewModel = viewModel,
                onBackClick = { viewModel.navigateBack() }
            )
        }
        is AppScreen.LinkedDevices -> {
            LinkedDevicesScreen(
                viewModel = viewModel,
                onBackClick = { viewModel.navigateBack() }
            )
        }
        is AppScreen.ChannelsAndCommunities -> {
            ChannelsAndCommunitiesScreen(
                viewModel = viewModel,
                onBackClick = { viewModel.navigateBack() }
            )
        }
        is AppScreen.SmartStorage -> {
            SmartStorageScreen(
                viewModel = viewModel,
                onBackClick = { viewModel.navigateBack() }
            )
        }
        is AppScreen.AdvancedProfile -> {
            AdvancedProfileScreen(
                viewModel = viewModel,
                onBackClick = { viewModel.navigateBack() }
            )
        }
        is AppScreen.SecurityCenter -> {
            SecurityCenterScreen(
                viewModel = viewModel,
                onBackClick = { viewModel.navigateBack() }
            )
        }
        is AppScreen.SupportTickets -> {
            AdminDashboardScreen(
                viewModel = viewModel,
                onBackClick = { viewModel.navigateBack() }
            )
        }
        is AppScreen.AppLock -> {
            AppLockScreen(viewModel = viewModel)
        }
        is AppScreen.Auth -> {
            AuthScreen(viewModel = viewModel)
        }
    }
}
