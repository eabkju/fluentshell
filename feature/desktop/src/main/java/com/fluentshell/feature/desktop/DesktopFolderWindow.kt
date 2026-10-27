package com.fluentshell.feature.desktop

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.fluentshell.core.data.model.AppInfo
import com.fluentshell.core.design.components.FluentWindow
import com.fluentshell.core.design.theme.LocalFluentColors

@Composable
fun DesktopFolderWindow(
    folderName: String,
    isOpen: Boolean,
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onClose: () -> Unit
) {
    FluentWindow(
        title = folderName,
        icon = Icons.Default.Folder,
        isOpen = isOpen,
        onClose = onClose,
        onMinimize = onClose,
        initialWidth = 540,
        initialHeight = 420
    ) {
        val colors = LocalFluentColors.current

        if (apps.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = colors.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This folder is empty",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 80.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(apps, key = { it.packageName }) { app ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onAppClick(app) }
                            .padding(8.dp)
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
                                modifier = Modifier.size(40.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = app.label,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = app.label,
                            fontSize = 11.sp,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
