package com.fluentshell.feature.desktop

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.fluentshell.core.data.model.AppInfo
import com.fluentshell.core.design.theme.LocalFluentColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesktopScreen(
    apps: List<AppInfo>,
    wallpaperUri: String? = null,
    onAppClick: (AppInfo) -> Unit,
    onRefresh: () -> Unit,
    onOpenSettings: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalFluentColors.current
    var isContextMenuVisible by remember { mutableStateOf(false) }
    var contextMenuOffset by remember { mutableStateOf(DpOffset.Zero) }

    // Desktop background: Windows 11 Fluent style gradient or custom wallpaper
    val defaultGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A),
            Color(0xFF1E293B),
            Color(0xFF090D16)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(defaultGradient)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.key == Key.F5) {
                    onRefresh()
                    true
                } else {
                    false
                }
            }
            .combinedClickable(
                onLongClick = {
                    contextMenuOffset = DpOffset(100.dp, 100.dp)
                    isContextMenuVisible = true
                },
                onClick = {
                    isContextMenuVisible = false
                }
            )
    ) {
        // App Grid on Desktop
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 90.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(apps.take(24), key = { it.packageName }) { app ->
                DesktopAppIcon(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = {
                        contextMenuOffset = DpOffset(60.dp, 60.dp)
                        isContextMenuVisible = true
                    }
                )
            }
        }

        // Desktop Context Menu (View, Sort, Refresh, Display, Personalize)
        DropdownMenu(
            expanded = isContextMenuVisible,
            onDismissRequest = { isContextMenuVisible = false },
            offset = contextMenuOffset,
            modifier = Modifier
                .background(colors.cardSurface)
                .clip(RoundedCornerShape(8.dp))
        ) {
            DropdownMenuItem(
                text = { Text("Refresh", color = colors.textPrimary) },
                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = colors.accent) },
                onClick = {
                    isContextMenuVisible = false
                    onRefresh()
                }
            )
            DropdownMenuItem(
                text = { Text("Display settings", color = colors.textPrimary) },
                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = colors.textSecondary) },
                onClick = {
                    isContextMenuVisible = false
                    onOpenSettings("DISPLAY")
                }
            )
            DropdownMenuItem(
                text = { Text("Personalize", color = colors.textPrimary) },
                leadingIcon = { Icon(Icons.Default.Apps, contentDescription = null, tint = colors.textSecondary) },
                onClick = {
                    isContextMenuVisible = false
                    onOpenSettings("PERSONALIZATION")
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesktopAppIcon(
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
            .size(84.dp)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(6.dp)
    ) {
        val bitmap = remember(app.icon) {
            try {
                app.icon?.toBitmap(64, 64, Bitmap.Config.ARGB_8888)
            } catch (_: Exception) {
                null
            }
        }

        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier.size(44.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = app.label,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Text(
            text = app.label,
            color = Color.White,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
