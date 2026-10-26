package com.fluentshell.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class FluentColorPalette(
    val micaBase: Color,
    val acrylic: Color,
    val cardSurface: Color,
    val cardHover: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color
)

val LocalFluentColors = staticCompositionLocalOf {
    FluentColorPalette(
        micaBase = DarkMicaBase,
        acrylic = DarkAcrylic,
        cardSurface = DarkCardSurface,
        cardHover = DarkCardHover,
        borderSubtle = DarkBorderSubtle,
        textPrimary = DarkTextPrimary,
        textSecondary = DarkTextSecondary,
        accent = WindowsBlue
    )
}

@Composable
fun FluentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: Color = WindowsBlue,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) {
        FluentColorPalette(
            micaBase = DarkMicaBase,
            acrylic = DarkAcrylic,
            cardSurface = DarkCardSurface,
            cardHover = DarkCardHover,
            borderSubtle = DarkBorderSubtle,
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            accent = accentColor
        )
    } else {
        FluentColorPalette(
            micaBase = LightMicaBase,
            acrylic = LightAcrylic,
            cardSurface = LightCardSurface,
            cardHover = LightCardHover,
            borderSubtle = LightBorderSubtle,
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            accent = accentColor
        )
    }

    val materialScheme = if (darkTheme) {
        darkColorScheme(
            primary = accentColor,
            background = palette.micaBase,
            surface = palette.cardSurface,
            onPrimary = Color.White,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            background = palette.micaBase,
            surface = palette.cardSurface,
            onPrimary = Color.White,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary
        )
    }

    CompositionLocalProvider(LocalFluentColors provides palette) {
        MaterialTheme(
            colorScheme = materialScheme,
            content = content
        )
    }
}
