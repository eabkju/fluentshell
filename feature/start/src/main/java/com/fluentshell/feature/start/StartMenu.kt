package com.fluentshell.feature.start

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.fluentshell.core.data.model.AppInfo
import com.fluentshell.core.design.components.MicaSurface
import com.fluentshell.core.design.theme.LocalFluentColors

@Composable
fun StartMenu(
    isOpen: Boolean,
    allApps: List<AppInfo>,
    pinnedApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onPinToStart: (AppInfo) -> Unit,
    onUnpinFromStart: (String) -> Unit,
    onPinToTaskbar: (AppInfo) -> Unit,
    onUnpinFromTaskbar: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalFluentColors.current
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var showAllAppsView by remember { mutableStateOf(false) }
    var isPowerMenuOpen by remember { mutableStateOf(false) }

    var selectedAppForMenu by remember { mutableStateOf<AppInfo?>(null) }
    var appMenuOffset by remember { mutableStateOf(DpOffset.Zero) }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 }),
        modifier = modifier
    ) {
        MicaSurface(
            modifier = Modifier
                .width(560.dp)
                .height(600.dp)
                .clip(RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            elevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp, bottom = 12.dp, start = 24.dp, end = 24.dp)
            ) {
                // Top Search Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.cardSurface)
                        .padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = colors.textPrimary, fontSize = 13.sp),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Type here to search...",
                                    color = colors.textSecondary,
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Body Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (searchQuery.isNotEmpty()) {
                        // Search results
                        val filtered = allApps.filter {
                            it.label.contains(searchQuery, ignoreCase = true) ||
                                it.packageName.contains(searchQuery, ignoreCase = true)
                        }
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filtered, key = { it.packageName }) { app ->
                                StartMenuAppRow(
                                    app = app,
                                    onClick = {
                                        onAppClick(app)
                                        onDismissRequest()
                                    },
                                    onLongClick = {
                                        selectedAppForMenu = app
                                        appMenuOffset = DpOffset(100.dp, 0.dp)
                                    }
                                )
                            }
                        }
                    } else if (showAllAppsView) {
                        // All Apps A-Z view
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { showAllAppsView = false }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = colors.textPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Back",
                                        color = colors.textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "All Apps (${allApps.size})",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(allApps, key = { it.packageName }) { app ->
                                    StartMenuAppRow(
                                        app = app,
                                        onClick = {
                                            onAppClick(app)
                                            onDismissRequest()
                                        },
                                        onLongClick = {
                                            selectedAppForMenu = app
                                            appMenuOffset = DpOffset(100.dp, 0.dp)
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // Pinned + Recommended Default View
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Pinned Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Pinned",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { showAllAppsView = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "All apps",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Pinned Grid (6 columns)
                            val displayPinned = if (pinnedApps.isNotEmpty()) pinnedApps else allApps.take(18)
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(6),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                            ) {
                                items(displayPinned, key = { it.packageName }) { app ->
                                    StartMenuAppItem(
                                        app = app,
                                        onClick = {
                                            onAppClick(app)
                                            onDismissRequest()
                                        },
                                        onLongClick = {
                                            selectedAppForMenu = app
                                            appMenuOffset = DpOffset(40.dp, 40.dp)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Recommended Header
                            Text(
                                text = "Recommended",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Recommended List (recent apps)
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(allApps.take(4), key = { it.packageName }) { app ->
                                    StartMenuAppRow(
                                        app = app,
                                        onClick = {
                                            onAppClick(app)
                                            onDismissRequest()
                                        },
                                        onLongClick = {
                                            selectedAppForMenu = app
                                            appMenuOffset = DpOffset(100.dp, 0.dp)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = colors.borderSubtle,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                // Bottom User & Power Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // User Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tablet User",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Power Button & Popup
                    Box {
                        IconButton(
                            onClick = { isPowerMenuOpen = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Power",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isPowerMenuOpen,
                            onDismissRequest = { isPowerMenuOpen = false },
                            modifier = Modifier.background(colors.cardSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sleep / Lock", color = colors.textPrimary) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = colors.textSecondary) },
                                onClick = {
                                    isPowerMenuOpen = false
                                    onDismissRequest()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Restart", color = colors.textPrimary) },
                                leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null, tint = colors.textSecondary) },
                                onClick = {
                                    isPowerMenuOpen = false
                                    onDismissRequest()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Shut down", color = colors.textPrimary) },
                                leadingIcon = { Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = colors.accent) },
                                onClick = {
                                    isPowerMenuOpen = false
                                    onDismissRequest()
                                }
                            )
                        }
                    }
                }
            }

            // Context Menu on App Long Press
            selectedAppForMenu?.let { app ->
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { selectedAppForMenu = null },
                    offset = appMenuOffset,
                    modifier = Modifier.background(colors.cardSurface)
                ) {
                    val isStartPinned = app.isPinnedToStart
                    DropdownMenuItem(
                        text = { Text(if (isStartPinned) "Unpin from Start" else "Pin to Start", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = colors.textSecondary) },
                        onClick = {
                            if (isStartPinned) onUnpinFromStart(app.packageName) else onPinToStart(app)
                            selectedAppForMenu = null
                        }
                    )

                    val isTaskbarPinned = app.isPinnedToTaskbar
                    DropdownMenuItem(
                        text = { Text(if (isTaskbarPinned) "Unpin from taskbar" else "Pin to taskbar", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = colors.textSecondary) },
                        onClick = {
                            if (isTaskbarPinned) onUnpinFromTaskbar(app.packageName) else onPinToTaskbar(app)
                            selectedAppForMenu = null
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("App info", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = colors.textSecondary) },
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${app.packageName}")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                            selectedAppForMenu = null
                            onDismissRequest()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Uninstall", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = colors.accent) },
                        onClick = {
                            val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                                data = Uri.parse("package:${app.packageName}")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                            selectedAppForMenu = null
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StartMenuAppItem(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalFluentColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp, horizontal = 4.dp)
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
                modifier = Modifier.size(34.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = app.label,
                tint = colors.textPrimary,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = app.label,
            color = colors.textPrimary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StartMenuAppRow(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalFluentColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        val bitmap = remember(app.icon) {
            try {
                app.icon?.toBitmap(40, 40, Bitmap.Config.ARGB_8888)
            } catch (_: Exception) {
                null
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = app.label,
                tint = colors.textPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = app.label,
                color = colors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.packageName,
                color = colors.textSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
