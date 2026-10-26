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

data class UserPreferences(
    val themeMode: ThemeMode,
    val accentColor: Long,
    val isMicaBlurEnabled: Boolean,
    val taskbarAlignment: TaskbarAlignment,
    val wallpaperUri: String?,
    val isFirstRun: Boolean
)

class ThemePreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = longPreferencesKey("accent_color")
        val MICA_BLUR_ENABLED = booleanPreferencesKey("mica_blur_enabled")
        val TASKBAR_ALIGNMENT = stringPreferencesKey("taskbar_alignment")
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
