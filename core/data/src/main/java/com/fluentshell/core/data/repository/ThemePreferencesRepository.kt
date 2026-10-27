package com.fluentshell.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fluent_settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class TaskbarAlignment {
    CENTER, LEFT
}

enum class WallpaperPreset(val id: String, val displayName: String, val colors: List<Long>) {
    BLOOM_DARK("bloom_dark", "Windows Bloom (Dark)", listOf(0xFF0D1B2A, 0xFF1B263B, 0xFF0A0F1D)),
    BLOOM_LIGHT("bloom_light", "Windows Bloom (Light)", listOf(0xFFD6E6F2, 0xFFEFF6FB, 0xFFE3EDF7)),
    SUNRISE_GLOW("sunrise_glow", "Sunrise Glow", listOf(0xFF2E1752, 0xFF5C2D6D, 0xFF130A24)),
    MINT_CALM("mint_calm", "Mint Calm", listOf(0xFF0E3830, 0xFF1C5243, 0xFF071C17)),
    SLATE_MINIMAL("slate_minimal", "Slate Minimal", listOf(0xFF1E293B, 0xFF334155, 0xFF0F172A))
}

data class UserPreferences(
    val themeMode: ThemeMode,
    val accentColor: Long,
    val isMicaBlurEnabled: Boolean,
    val taskbarAlignment: TaskbarAlignment,
    val wallpaperPreset: String,
    val wallpaperUri: String?,
    val isFirstRun: Boolean
)

class ThemePreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = longPreferencesKey("accent_color")
        val MICA_BLUR_ENABLED = booleanPreferencesKey("mica_blur_enabled")
        val TASKBAR_ALIGNMENT = stringPreferencesKey("taskbar_alignment")
        val WALLPAPER_PRESET = stringPreferencesKey("wallpaper_preset")
        val WALLPAPER_URI = stringPreferencesKey("wallpaper_uri")
        val FIRST_RUN = booleanPreferencesKey("is_first_run")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val themeModeStr = prefs[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }

        val alignmentStr = prefs[PreferencesKeys.TASKBAR_ALIGNMENT] ?: TaskbarAlignment.CENTER.name
        val taskbarAlignment = try {
            TaskbarAlignment.valueOf(alignmentStr)
        } catch (_: Exception) {
            TaskbarAlignment.CENTER
        }

        UserPreferences(
            themeMode = themeMode,
            accentColor = prefs[PreferencesKeys.ACCENT_COLOR] ?: 0xFF0078D4,
            isMicaBlurEnabled = prefs[PreferencesKeys.MICA_BLUR_ENABLED] ?: true,
            taskbarAlignment = taskbarAlignment,
            wallpaperPreset = prefs[PreferencesKeys.WALLPAPER_PRESET] ?: WallpaperPreset.BLOOM_DARK.id,
            wallpaperUri = prefs[PreferencesKeys.WALLPAPER_URI],
            isFirstRun = prefs[PreferencesKeys.FIRST_RUN] ?: true
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun setAccentColor(color: Long) {
        context.dataStore.edit { it[PreferencesKeys.ACCENT_COLOR] = color }
    }

    suspend fun setMicaBlurEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.MICA_BLUR_ENABLED] = enabled }
    }

    suspend fun setTaskbarAlignment(alignment: TaskbarAlignment) {
        context.dataStore.edit { it[PreferencesKeys.TASKBAR_ALIGNMENT] = alignment.name }
    }

    suspend fun setWallpaperPreset(presetId: String) {
        context.dataStore.edit {
            it[PreferencesKeys.WALLPAPER_PRESET] = presetId
            it.remove(PreferencesKeys.WALLPAPER_URI)
        }
    }

    suspend fun setWallpaperUri(uri: String?) {
        context.dataStore.edit {
            if (uri != null) {
                it[PreferencesKeys.WALLPAPER_URI] = uri
            } else {
                it.remove(PreferencesKeys.WALLPAPER_URI)
            }
        }
    }

    suspend fun setFirstRunCompleted() {
        context.dataStore.edit { it[PreferencesKeys.FIRST_RUN] = false }
    }
}
