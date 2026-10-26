package com.fluentshell.core.design.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fluentshell.core.design.theme.FluentShapeMedium
import com.fluentshell.core.design.theme.LocalFluentColors

@Composable
fun MicaSurface(
    modifier: Modifier = Modifier,
    shape: Shape = FluentShapeMedium,
    elevation: Dp = 8.dp,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    enableBlur: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalFluentColors.current
    val surfaceColor = backgroundColor ?: colors.acrylic
    val borderCol = borderColor ?: colors.borderSubtle

    Box(
        modifier = modifier
            .shadow(elevation = elevation, shape = shape, clip = false)
            .clip(shape)
            .background(surfaceColor)
            .border(width = borderWidth, color = borderCol, shape = shape),
        content = content
    )
}

@Composable
fun FluentCard(
    modifier: Modifier = Modifier,
    shape: Shape = FluentShapeMedium,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalFluentColors.current
    val interactionSource = remember { MutableInteractionSource() }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(color = colors.accent.copy(alpha = 0.2f)),
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.cardSurface)
            .border(1.dp, colors.borderSubtle, shape)
            .then(clickableModifier),
        content = content
    )
}
