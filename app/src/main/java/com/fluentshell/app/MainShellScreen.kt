package com.fluentshell.app

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluentshell.core.data.model.AppInfo
import com.fluentshell.core.data.repository.TaskbarAlignment
import com.fluentshell.core.data.repository.ThemeMode
import com.fluentshell.core.data.repository.UserPreferences
import com.fluentshell.core.design.components.MicaSurface
import com.fluentshell.core.design.theme.FluentTheme
import com.fluentshell.core.design.theme.LocalFluentColors
import com.fluentshell.core.design.theme.WindowsBlue
import com.fluentshell.feature.desktop.DesktopScreen
import com.fluentshell.feature.notifications.ActionCenterFlyout
import com.fluentshell.feature.notifications.QuickSettingsFlyout
import com.fluentshell.feature.notifications.ToastPopup
import com.fluentshell.feature.settings.SettingsScreen
import com.fluentshell.feature.start.StartMenu
import com.fluentshell.feature.taskbar.FluentTaskbar
import kotlinx.coroutines.launch

@Composable
fun MainShellScreen(
    app: FluentApplication,
    initialOpenSettings: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userPreferences by app.themeRepository.userPreferencesFlow.collectAsState(
        initial = UserPreferences(
            themeMode = ThemeMode.SYSTEM,
            accentColor = 0xFF0078D4,
            isMicaBlurEnabled = true,
            taskbarAlignment = TaskbarAlignment.CENTER,
            wallpaperUri = null,
            isFirstRun = false
        )
    )

    val allApps by app.appRepository.installedApps.collectAsState()
    val notifications by app.notificationStore.notifications.collectAsState()
    val currentToast by app.notificationStore.currentToast.collectAsState()

    var isStartMenuOpen by remember { mutableStateOf(false) }
    var isQuickSettingsOpen by remember { mutableStateOf(false) }
    var isActionCenterOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(initialOpenSettings) }
    var showFirstRunWizard by remember(userPreferences.isFirstRun) { mutableStateOf(userPreferences.isFirstRun) }

    val darkTheme = when (userPreferences.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val accentColor = Color(userPreferences.accentColor.toULong())

    FluentTheme(
        darkTheme = darkTheme,
        accentColor = accentColor
    ) {
        val colors = LocalFluentColors.current

        // Launch app helper
        val launchApp: (AppInfo) -> Unit = { appInfo ->
            val intent = context.packageManager.getLaunchIntentForPackage(appInfo.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }

        val dismissAllFlyouts = {
            isStartMenuOpen = false
            isQuickSettingsOpen = false
            isActionCenterOpen = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = dismissAllFlyouts
                )
        ) {
            // Desktop Area
            DesktopScreen(
                apps = allApps,
                wallpaperUri = userPreferences.wallpaperUri,
                onAppClick = launchApp,
                onRefresh = { app.appRepository.refreshApps() },
                onOpenSettings = {
                    dismissAllFlyouts()
                    isSettingsOpen = true
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 52.dp)
            )

            // Start Menu Flyout (Anchored above taskbar)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = if (userPreferences.taskbarAlignment == TaskbarAlignment.CENTER) Alignment.BottomCenter else Alignment.BottomStart
            ) {
                StartMenu(
                    isOpen = isStartMenuOpen,
                    allApps = allApps,
                    pinnedApps = allApps.filter { it.isPinnedToStart },
                    onAppClick = launchApp,
                    onPinToStart = { scope.launch { app.appRepository.pinAppToStart(it) } },
                    onUnpinFromStart = { scope.launch { app.appRepository.unpinAppFromStart(it) } },
                    onPinToTaskbar = { scope.launch { app.appRepository.pinAppToTaskbar(it) } },
                    onUnpinFromTaskbar = { scope.launch { app.appRepository.unpinAppFromTaskbar(it) } },
                    onDismissRequest = { isStartMenuOpen = false },
                    modifier = Modifier.padding(start = if (userPreferences.taskbarAlignment == TaskbarAlignment.LEFT) 12.dp else 0.dp)
                )
            }

            // Quick Settings Flyout (Anchored above tray)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp, end = 12.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                QuickSettingsFlyout(
                    isOpen = isQuickSettingsOpen,
                    batteryLevel = app.systemSettingsGateway.getBatteryLevel(),
                    systemSettingsGateway = app.systemSettingsGateway,
                    onOpenFullSettings = {
                        isQuickSettingsOpen = false
                        isSettingsOpen = true
                    }
                )
            }

            // Action Center Flyout (Anchored above tray)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp, end = 12.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                ActionCenterFlyout(
                    isOpen = isActionCenterOpen,
                    notifications = notifications,
                    onDismissNotification = { app.notificationStore.dismissNotification(it) },
                    onClearAll = { app.notificationStore.clearAll() }
                )
            }

            // Windows-style Toast Popup (Bottom Right)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                ToastPopup(
                    toast = currentToast,
                    onDismiss = { app.notificationStore.dismissToast() }
                )
            }

            // Full Settings Window (Control Panel)
            AnimatedVisibility(
                visible = isSettingsOpen,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                SettingsScreen(
                    userPreferences = userPreferences,
                    themeRepository = app.themeRepository,
                    systemSettingsGateway = app.systemSettingsGateway,
                    onClose = { isSettingsOpen = false }
                )
            }

            // First-run Onboarding Dialog
            if (showFirstRunWizard) {
                FirstRunWizardDialog(
                    onComplete = {
                        scope.launch {
                            app.themeRepository.setFirstRunCompleted()
                            showFirstRunWizard = false
                        }
                    }
                )
            }

            // Taskbar (Bottom)
            FluentTaskbar(
                alignment = userPreferences.taskbarAlignment,
                pinnedApps = allApps.filter { it.isPinnedToTaskbar },
                isStartMenuOpen = isStartMenuOpen,
                isQuickSettingsOpen = isQuickSettingsOpen,
                isActionCenterOpen = isActionCenterOpen,
                batteryLevel = app.systemSettingsGateway.getBatteryLevel(),
                unreadNotificationCount = notifications.size,
                onStartClick = {
                    isStartMenuOpen = !isStartMenuOpen
                    isQuickSettingsOpen = false
                    isActionCenterOpen = false
                },
                onSearchClick = {
                    isStartMenuOpen = true
                    isQuickSettingsOpen = false
                    isActionCenterOpen = false
                },
                onAppClick = launchApp,
                onQuickSettingsClick = {
                    isQuickSettingsOpen = !isQuickSettingsOpen
                    isStartMenuOpen = false
                    isActionCenterOpen = false
                },
                onActionCenterClick = {
                    isActionCenterOpen = !isActionCenterOpen
                    isStartMenuOpen = false
                    isQuickSettingsOpen = false
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun FirstRunWizardDialog(
    onComplete: () -> Unit
) {
    val colors = LocalFluentColors.current
    val context = LocalContext.current

    val roleManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.getSystemService(RoleManager::class.java)
    } else null

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        MicaSurface(
            modifier = Modifier
                .width(440.dp)
                .clip(RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            elevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Window,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Welcome to FluentShell",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Text(
                    text = "Windows 11 experience for your Android tablet.",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Set Default Home Button
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && roleManager?.isRoleAvailable(RoleManager.ROLE_HOME) == true) {
                            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                            roleLauncher.launch(intent)
                        } else {
                            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set as Default Launcher")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grant Notification Access
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cardSurface),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Grant Notification Access", color = colors.textPrimary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grant Write Settings
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                            data = android.net.Uri.parse("package:${context.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cardSurface),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Grant System Settings Access", color = colors.textPrimary)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.cardHover),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Get Started", color = colors.textPrimary)
                }
            }
        }
    }
}
