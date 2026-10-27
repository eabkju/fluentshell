package com.fluentshell.feature.settings

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluentshell.core.data.repository.TaskbarAlignment
import com.fluentshell.core.data.repository.ThemeMode
import com.fluentshell.core.data.repository.ThemePreferencesRepository
import com.fluentshell.core.data.repository.UserPreferences
import com.fluentshell.core.design.components.FluentCard
import com.fluentshell.core.design.components.MicaSurface
import com.fluentshell.core.design.theme.FluentAccentList
import com.fluentshell.core.design.theme.LocalFluentColors
import com.fluentshell.core.system.SettingResult
import com.fluentshell.core.system.SystemSettingsGateway
import kotlinx.coroutines.launch

enum class SettingsCategory {
    SYSTEM, NETWORK, BLUETOOTH, PERSONALIZATION, APPS, ABOUT
}

@Composable
fun SettingsScreen(
    userPreferences: UserPreferences,
    themeRepository: ThemePreferencesRepository,
    systemSettingsGateway: SystemSettingsGateway,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalFluentColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf(SettingsCategory.SYSTEM) }

    MicaSurface(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        shape = RoundedCornerShape(10.dp),
        elevation = 16.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Window Title Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(colors.cardSurface)
                    .padding(horizontal = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Settings",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Dual Pane Body
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Left Navigation Sidebar
                Column(
                    modifier = Modifier
                        .width(240.dp)
                        .fillMaxHeight()
                        .background(colors.micaBase.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SettingsNavItem(
                        title = "System",
                        icon = Icons.Default.Computer,
                        isSelected = selectedCategory == SettingsCategory.SYSTEM,
                        onClick = { selectedCategory = SettingsCategory.SYSTEM }
                    )
                    SettingsNavItem(
                        title = "Network & internet",
                        icon = Icons.Default.Wifi,
                        isSelected = selectedCategory == SettingsCategory.NETWORK,
                        onClick = { selectedCategory = SettingsCategory.NETWORK }
                    )
                    SettingsNavItem(
                        title = "Bluetooth & devices",
                        icon = Icons.Default.Bluetooth,
                        isSelected = selectedCategory == SettingsCategory.BLUETOOTH,
                        onClick = { selectedCategory = SettingsCategory.BLUETOOTH }
                    )
                    SettingsNavItem(
                        title = "Personalization",
                        icon = Icons.Default.ColorLens,
                        isSelected = selectedCategory == SettingsCategory.PERSONALIZATION,
                        onClick = { selectedCategory = SettingsCategory.PERSONALIZATION }
                    )
                    SettingsNavItem(
                        title = "Apps",
                        icon = Icons.Default.Apps,
                        isSelected = selectedCategory == SettingsCategory.APPS,
                        onClick = { selectedCategory = SettingsCategory.APPS }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    SettingsNavItem(
                        title = "About FluentShell",
                        icon = Icons.Default.Info,
                        isSelected = selectedCategory == SettingsCategory.ABOUT,
                        onClick = { selectedCategory = SettingsCategory.ABOUT }
                    )
                }

                // Right Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedCategory) {
                        SettingsCategory.SYSTEM -> SystemSettingsPage(systemSettingsGateway, context)
                        SettingsCategory.NETWORK -> NetworkSettingsPage(systemSettingsGateway, context)
                        SettingsCategory.BLUETOOTH -> BluetoothSettingsPage(systemSettingsGateway, context)
                        SettingsCategory.PERSONALIZATION -> PersonalizationSettingsPage(userPreferences, themeRepository, scope)
                        SettingsCategory.APPS -> AppsSettingsPage(systemSettingsGateway, context)
                        SettingsCategory.ABOUT -> AboutSettingsPage()
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalFluentColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) colors.cardHover else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) colors.accent else colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = colors.textPrimary
        )
    }
}

@Composable
fun SystemSettingsPage(
    gateway: SystemSettingsGateway,
    context: Context
) {
    val colors = LocalFluentColors.current
    var brightness by remember { mutableFloatStateOf(gateway.getBrightness()) }
    var volume by remember { mutableFloatStateOf(gateway.getMediaVolume()) }
    var autoRotate by remember { mutableStateOf(gateway.isAutoRotationEnabled()) }
    var torchEnabled by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("System", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)

        // Display Brightness
        FluentCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = colors.accent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Display Brightness", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    Spacer(modifier = Modifier.weight(1f))
                    SettingBadge(isDirect = true)
                }
                Slider(
                    value = brightness,
                    onValueChange = {
                        brightness = it
                        val res = gateway.setBrightness(it)
                        if (res is SettingResult.NeedsPermission) {
                            context.startActivity(res.intent)
                        }
                    },
                    colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent)
                )
            }
        }

        // Sound / Volume
        FluentCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = colors.accent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Media Volume", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    Spacer(modifier = Modifier.weight(1f))
                    SettingBadge(isDirect = true)
                }
                Slider(
                    value = volume,
                    onValueChange = {
                        volume = it
                        gateway.setMediaVolume(it)
                    },
                    colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent)
                )
            }
        }

        // Auto Rotation
        FluentCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.ScreenRotation, contentDescription = null, tint = colors.accent)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Auto-rotate screen", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    Text("Automatically rotate when device orientation changes", fontSize = 11.sp, color = colors.textSecondary)
                }
                Spacer(modifier = Modifier.weight(1f))
                SettingBadge(isDirect = true)
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = autoRotate,
                    onCheckedChange = {
                        autoRotate = it
                        val res = gateway.setAutoRotation(it)
                        if (res is SettingResult.NeedsPermission) {
                            context.startActivity(res.intent)
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = colors.accent)
                )
            }
        }

        // Flashlight
        FluentCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.FlashlightOn, contentDescription = null, tint = colors.accent)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Flashlight", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    Text("Toggle rear camera torch", fontSize = 11.sp, color = colors.textSecondary)
                }
                Spacer(modifier = Modifier.weight(1f))
                SettingBadge(isDirect = true)
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = torchEnabled,
                    onCheckedChange = {
                        torchEnabled = it
                        gateway.toggleTorch(it)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = colors.accent)
                )
            }
        }
    }
}

@Composable
fun NetworkSettingsPage(gateway: SystemSettingsGateway, context: Context) {
    val colors = LocalFluentColors.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Network & internet", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)

        DeepLinkCard(
            title = "Wi-Fi",
            subtitle = "Connect to wireless networks, view saved networks",
            icon = Icons.Default.Wifi,
            onClick = {
                val res = gateway.openWifiSettings()
                if (res is SettingResult.DeepLink) context.startActivity(res.intent)
            }
        )

        DeepLinkCard(
            title = "Airplane mode",
            subtitle = "Disable Wi-Fi, Bluetooth, and cellular signals",
            icon = Icons.Default.OpenInNew,
            onClick = {
                val res = gateway.openAirplaneModeSettings()
                if (res is SettingResult.DeepLink) context.startActivity(res.intent)
            }
        )
    }
}

@Composable
fun BluetoothSettingsPage(gateway: SystemSettingsGateway, context: Context) {
    val colors = LocalFluentColors.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Bluetooth & devices", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)

        DeepLinkCard(
            title = "Bluetooth settings",
            subtitle = "Pair new devices, keyboards, mice, audio accessories",
            icon = Icons.Default.Bluetooth,
            onClick = {
                val res = gateway.openBluetoothSettings()
                if (res is SettingResult.DeepLink) context.startActivity(res.intent)
            }
        )
    }
}

@Composable
fun PersonalizationSettingsPage(
    userPreferences: UserPreferences,
    themeRepository: ThemePreferencesRepository,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val colors = LocalFluentColors.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Personalization", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)

        // Theme Mode (Light / Dark / System)
        FluentCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Theme mode", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        val isSelected = userPreferences.themeMode == mode
                        Button(
                            onClick = { scope.launch { themeRepository.setThemeMode(mode) } },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) colors.accent else colors.cardHover,
                                contentColor = if (isSelected) Color.White else colors.textPrimary
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }
        }

        // Accent Color Chooser
        FluentCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Accent color", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FluentAccentList.forEach { color ->
                        val colorArgb = color.toArgb().toLong() and 0xFFFFFFFFL
                        val isSelected = (userPreferences.accentColor and 0xFFFFFFFFL) == colorArgb
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    scope.launch { themeRepository.setAccentColor(colorArgb) }
                                }
                        )
                    }
                }
            }
        }

        // Taskbar Alignment (Center vs Left)
        FluentCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Column {
                    Text("Taskbar alignment", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    Text("Choose between Windows 11 centered or classic left", fontSize = 11.sp, color = colors.textSecondary)
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskbarAlignment.entries.forEach { alignment ->
                        val isSelected = userPreferences.taskbarAlignment == alignment
                        Button(
                            onClick = { scope.launch { themeRepository.setTaskbarAlignment(alignment) } },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) colors.accent else colors.cardHover,
                                contentColor = if (isSelected) Color.White else colors.textPrimary
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(alignment.name)
                        }
                    }
                }
            }
        }

        // Mica Blur Toggle
        FluentCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Column {
                    Text("Acrylic & Mica blur effect", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    Text("Enable translucent blur surfaces on supported devices", fontSize = 11.sp, color = colors.textSecondary)
                }
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = userPreferences.isMicaBlurEnabled,
                    onCheckedChange = { scope.launch { themeRepository.setMicaBlurEnabled(it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = colors.accent)
                )
            }
        }
    }
}

@Composable
fun AppsSettingsPage(gateway: SystemSettingsGateway, context: Context) {
    val colors = LocalFluentColors.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Apps", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)

        DeepLinkCard(
            title = "Default apps",
            subtitle = "Choose default launcher, browser, and assistant apps",
            icon = Icons.Default.Apps,
            onClick = {
                val res = gateway.openDefaultAppsSettings()
                if (res is SettingResult.DeepLink) context.startActivity(res.intent)
            }
        )

        DeepLinkCard(
            title = "Notification access",
            subtitle = "Enable notification mirroring for toasts and action center",
            icon = Icons.Default.OpenInNew,
            onClick = {
                val res = gateway.openNotificationListenerSettings()
                if (res is SettingResult.DeepLink) context.startActivity(res.intent)
            }
        )
    }
}

@Composable
fun AboutSettingsPage() {
    val colors = LocalFluentColors.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("About FluentShell", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)

        FluentCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("FluentShell v1.0.0 (MVP)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Windows 11–style shell launcher designed for Android tablets.", fontSize = 12.sp, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Architecture: ARM64-v8a", fontSize = 12.sp, color = colors.textSecondary)
                Text("Target SDK: Android 15 (API 35)", fontSize = 12.sp, color = colors.textSecondary)
                Text("License: MIT Open Source", fontSize = 12.sp, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Disclaimer: FluentShell is not affiliated with, endorsed by, or sponsored by Microsoft Corporation. Windows is a trademark of Microsoft Corporation.",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun DeepLinkCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = LocalFluentColors.current

    FluentCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = colors.accent)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Text(subtitle, fontSize = 11.sp, color = colors.textSecondary)
            }
            Spacer(modifier = Modifier.weight(1f))
            SettingBadge(isDirect = false)
        }
    }
}

@Composable
fun SettingBadge(isDirect: Boolean) {
    val backgroundColor = if (isDirect) Color(0x22107C41) else Color(0x220078D4)
    val textColor = if (isDirect) Color(0xFF107C41) else Color(0xFF0078D4)
    val text = if (isDirect) "Applied" else "Opens Android settings"

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
