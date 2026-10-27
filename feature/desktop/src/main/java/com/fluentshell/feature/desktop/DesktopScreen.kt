package com.fluentshell.feature.desktop

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.fluentshell.core.data.model.AppInfo
import com.fluentshell.core.data.repository.WallpaperPreset
import com.fluentshell.core.design.theme.LocalFluentColors
import kotlin.math.roundToInt

data class DesktopFolder(
    val id: String,
    val name: String,
    val appPackageNames: List<String> = emptyList()
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesktopScreen(
    apps: List<AppInfo>,
    folders: List<DesktopFolder>,
    wallpaperPreset: String,
    wallpaperUri: String? = null,
    onAppClick: (AppInfo) -> Unit,
    onFolderClick: (DesktopFolder) -> Unit,
    onCreateFolder: () -> Unit,
    onRefresh: () -> Unit,
    onOpenSettings: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalFluentColors.current
    val density = LocalDensity.current
    var isContextMenuVisible by remember { mutableStateOf(false) }
    var contextMenuOffset by remember { mutableStateOf(DpOffset.Zero) }

    // Flicker-free cached background brush
    val backgroundBrush = remember(wallpaperPreset) {
        val preset = WallpaperPreset.entries.firstOrNull { it.id == wallpaperPreset } ?: WallpaperPreset.BLOOM_DARK
        Brush.verticalGradient(preset.colors.map { Color(it) })
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.key == Key.F5) {
                    onRefresh()
                    true
                } else {
                    false
                }
            }
            // Mouse Right-Click Detection (Secondary pointer button)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                            val pos = event.changes.first().position
                            with(density) {
                                contextMenuOffset = DpOffset(pos.x.toDp(), pos.y.toDp())
                            }
                            isContextMenuVisible = true
                        }
                    }
                }
            }
            // Touch Long Press Detection
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        with(density) {
                            contextMenuOffset = DpOffset(offset.x.toDp(), offset.y.toDp())
                        }
                        isContextMenuVisible = true
                    },
                    onTap = {
                        isContextMenuVisible = false
                    }
                )
            }
    ) {
        // Desktop Grid (Folders + Apps)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 92.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Folders on Desktop
            items(folders, key = { it.id }) { folder ->
                DraggableDesktopFolderIcon(
                    folder = folder,
                    onClick = { onFolderClick(folder) },
                    onLongClick = {
                        contextMenuOffset = DpOffset(40.dp, 40.dp)
                        isContextMenuVisible = true
                    }
                )
            }

            // Apps on Desktop
            items(apps.take(30), key = { it.packageName }) { app ->
                DraggableDesktopAppIcon(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = {
                        contextMenuOffset = DpOffset(40.dp, 40.dp)
                        isContextMenuVisible = true
                    }
                )
            }
        }

        // Windows 11 Desktop Context Menu
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
                text = { Text("New folder", color = colors.textPrimary) },
                leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = colors.textPrimary) },
                onClick = {
                    isContextMenuVisible = false
                    onCreateFolder()
                }
            )
            HorizontalDivider(color = colors.borderSubtle, modifier = Modifier.padding(vertical = 4.dp))
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
                leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, tint = colors.accent) },
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
fun DraggableDesktopAppIcon(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalFluentColors.current
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .size(86.dp)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(6.dp)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableDesktopFolderIcon(
    folder: DesktopFolder,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalFluentColors.current
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .size(86.dp)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = folder.name,
            tint = Color(0xFFFFB900), // Windows yellow folder color
            modifier = Modifier.size(44.dp)
        )

        Text(
            text = folder.name,
            color = Color.White,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
