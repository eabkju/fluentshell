package com.fluentshell.core.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fluentshell.core.design.theme.LocalFluentColors
import kotlin.math.roundToInt

@Composable
fun FluentWindow(
    title: String,
    icon: ImageVector? = null,
    isOpen: Boolean,
    onClose: () -> Unit,
    onMinimize: () -> Unit = {},
    modifier: Modifier = Modifier,
    initialWidth: Int = 800,
    initialHeight: Int = 580,
    content: @Composable BoxScope.() -> Unit
) {
    if (!isOpen) return

    val colors = LocalFluentColors.current
    var isMaximized by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + scaleIn(initialScale = 0.95f),
        exit = fadeOut() + scaleOut(targetScale = 0.95f),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = if (isMaximized) Alignment.TopStart else Alignment.Center
        ) {
            val windowModifier = if (isMaximized) {
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(0.dp))
            } else {
                Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .width(initialWidth.dp)
                    .height(initialHeight.dp)
                    .shadow(elevation = 24.dp, shape = RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
            }

            Column(
                modifier = windowModifier
                    .background(colors.cardSurface)
            ) {
                // Windows 11 Title Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(colors.micaBase.copy(alpha = 0.85f))
                        .pointerInput(isMaximized) {
                            if (!isMaximized) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount.x
                                    offsetY += dragAmount.y
                                }
                            }
                        }
                        .padding(start = 12.dp)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Title bar action buttons: Minimize, Maximize/Restore, Close
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Minimize
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(width = 44.dp, height = 38.dp)
                                .clickable(onClick = onMinimize)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Minimize",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Maximize / Restore
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(width = 44.dp, height = 38.dp)
                                .clickable { isMaximized = !isMaximized }
                        ) {
                            Icon(
                                imageVector = if (isMaximized) Icons.Default.FilterNone else Icons.Default.CropSquare,
                                contentDescription = if (isMaximized) "Restore" else "Maximize",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        // Close (turns red on hover/click)
                        var isCloseHovered by remember { mutableStateOf(false) }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(width = 46.dp, height = 38.dp)
                                .background(if (isCloseHovered) Color(0xFFC42B1C) else Color.Transparent)
                                .clickable(onClick = onClose)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = if (isCloseHovered) Color.White else colors.textPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Window Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    content = content
                )
            }
        }
    }
}
