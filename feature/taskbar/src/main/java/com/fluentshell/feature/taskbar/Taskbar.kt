package com.fluentshell.feature.taskbar

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.fluentshell.core.data.model.AppInfo
import com.fluentshell.core.data.repository.TaskbarAlignment
import com.fluentshell.core.design.components.MicaSurface
import com.fluentshell.core.design.theme.LocalFluentColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FluentTaskbar(
    alignment: TaskbarAlignment,
    pinnedApps: List<AppInfo>,
    isStartMenuOpen: Boolean,
    isQuickSettingsOpen: Boolean,
    isActionCenterOpen: Boolean,
    batteryLevel: Int,
    unreadNotificationCount: Int,
    onStartClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onQuickSettingsClick: () -> Unit,
    onActionCenterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalFluentColors.current

    // Live clock update
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            delay(1000)
        }
    }

    MicaSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
        elevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Centered or Left-aligned cluster
            if (alignment == TaskbarAlignment.CENTER) {
                Spacer(modifier = Modifier.weight(1f))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Windows 11 Start Button
                TaskbarIconButton(
                    isSelected = isStartMenuOpen,
                    onClick = onStartClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Window,
                        contentDescription = "Start",
                        tint = colors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Search Button
                TaskbarIconButton(
                    isSelected = false,
                    onClick = onSearchClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Task View / Grid
                TaskbarIconButton(
                    isSelected = false,
                    onClick = onStartClick
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Task View",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Pinned Apps Icons
                pinnedApps.forEach { app ->
                    TaskbarAppItem(
                        app = app,
                        onClick = { onAppClick(app) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // System Tray on Right
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Quick settings pill (Wifi, Volume, Battery)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isQuickSettingsOpen) colors.cardHover else Color.Transparent)
                        .clickable(onClick = onQuickSettingsClick)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Wi-Fi",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryFull,
                            contentDescription = "Battery",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$batteryLevel%",
                            fontSize = 11.sp,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Clock and Notification Bell
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isActionCenterOpen) colors.cardHover else Color.Transparent)
                        .clickable(onClick = onActionCenterClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = currentTime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = currentDate,
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }

                    BadgedBox(
                        badge = {
                            if (unreadNotificationCount > 0) {
                                Badge(
                                    containerColor = colors.accent,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskbarIconButton(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalFluentColors.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) colors.cardHover else Color.Transparent)
            .clickable(onClick = onClick)
    ) {
        content()

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(16.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.accent)
            )
        }
    }
}

@Composable
fun TaskbarAppItem(
    app: AppInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalFluentColors.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        val bitmap = remember(app.icon) {
            try {
                app.icon?.toBitmap(48, 48, Bitmap.Config.ARGB_8888)
            } catch (_: Exception) {
                null
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier.size(26.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = app.label,
                tint = colors.textPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
