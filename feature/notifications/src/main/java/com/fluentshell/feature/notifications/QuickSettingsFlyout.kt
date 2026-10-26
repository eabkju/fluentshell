package com.fluentshell.feature.notifications

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluentshell.core.design.components.MicaSurface
import com.fluentshell.core.design.theme.LocalFluentColors
import com.fluentshell.core.system.SettingResult
import com.fluentshell.core.system.SystemSettingsGateway

@Composable
fun QuickSettingsFlyout(
    isOpen: Boolean,
    batteryLevel: Int,
    systemSettingsGateway: SystemSettingsGateway,
    onOpenFullSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalFluentColors.current
    val context = LocalContext.current

    var brightness by remember { mutableFloatStateOf(systemSettingsGateway.getBrightness()) }
    var volume by remember { mutableFloatStateOf(systemSettingsGateway.getMediaVolume()) }
    var isFlashlightOn by remember { mutableStateOf(false) }
    var isAutoRotateOn by remember { mutableStateOf(systemSettingsGateway.isAutoRotationEnabled()) }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 }),
        modifier = modifier
    ) {
        MicaSurface(
            modifier = Modifier
                .width(360.dp)
                .clip(RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            elevation = 20.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Quick Tiles Grid (2 columns, 3 rows)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        QuickTile(
                            title = "Wi-Fi",
                            icon = Icons.Default.Wifi,
                            isActive = true,
                            onClick = {
                                val res = systemSettingsGateway.openWifiSettings()
                                if (res is SettingResult.DeepLink) context.startActivity(res.intent)
                            }
                        )
                    }
                    item {
                        QuickTile(
                            title = "Bluetooth",
                            icon = Icons.Default.Bluetooth,
                            isActive = true,
                            onClick = {
                                val res = systemSettingsGateway.openBluetoothSettings()
                                if (res is SettingResult.DeepLink) context.startActivity(res.intent)
                            }
                        )
                    }
                    item {
                        QuickTile(
                            title = "Airplane mode",
                            icon = Icons.Default.AirplanemodeActive,
                            isActive = false,
                            onClick = {
                                val res = systemSettingsGateway.openAirplaneModeSettings()
                                if (res is SettingResult.DeepLink) context.startActivity(res.intent)
                            }
                        )
                    }
                    item {
                        QuickTile(
                            title = "Flashlight",
                            icon = Icons.Default.FlashlightOn,
                            isActive = isFlashlightOn,
                            onClick = {
                                isFlashlightOn = !isFlashlightOn
                                systemSettingsGateway.toggleTorch(isFlashlightOn)
                            }
                        )
                    }
                    item {
                        QuickTile(
                            title = "Auto-rotate",
                            icon = Icons.Default.ScreenRotation,
                            isActive = isAutoRotateOn,
                            onClick = {
                                isAutoRotateOn = !isAutoRotateOn
                                val res = systemSettingsGateway.setAutoRotation(isAutoRotateOn)
                                if (res is SettingResult.NeedsPermission) {
                                    context.startActivity(res.intent)
                                }
                            }
                        )
                    }
                    item {
                        QuickTile(
                            title = "Battery saver",
                            icon = Icons.Default.BatteryFull,
                            isActive = false,
                            onClick = {
                                val res = systemSettingsGateway.openBatterySettings()
                                if (res is SettingResult.DeepLink) context.startActivity(res.intent)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Brightness Slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.BrightnessMedium,
                        contentDescription = "Brightness",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = brightness,
                        onValueChange = {
                            brightness = it
                            val res = systemSettingsGateway.setBrightness(it)
                            if (res is SettingResult.NeedsPermission) {
                                context.startActivity(res.intent)
                            }
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accent,
                            activeTrackColor = colors.accent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Volume Slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = volume,
                        onValueChange = {
                            volume = it
                            systemSettingsGateway.setMediaVolume(it)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accent,
                            activeTrackColor = colors.accent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(
                    color = colors.borderSubtle,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Bottom Status & Settings Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = "Battery",
                        tint = colors.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$batteryLevel% battery remaining",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = onOpenFullSettings,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "All Settings",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickTile(
    title: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalFluentColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) colors.accent else colors.cardSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) Color.White else colors.textPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) Color.White else colors.textPrimary
        )
    }
}
