package com.fluentshell.core.system

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.Settings
import kotlin.math.roundToInt

sealed interface SettingResult {
    data class Direct(val message: String? = null) : SettingResult
    data class NeedsPermission(val intent: Intent, val permissionName: String) : SettingResult
    data class DeepLink(val intent: Intent, val description: String) : SettingResult
}

class SystemSettingsGateway(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    // --- WRITE SETTINGS PERMISSION ---
    fun canWriteSettings(): Boolean {
        return Settings.System.canWrite(context)
    }

    private fun getWriteSettingsPermissionIntent(): Intent {
        return Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    // --- BRIGHTNESS (0.0 to 1.0) ---
    fun getBrightness(): Float {
        return try {
            val brightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
            (brightness / 255f).coerceIn(0f, 1f)
        } catch (_: Exception) {
            0.5f
        }
    }

    fun setBrightness(fraction: Float): SettingResult {
        if (!canWriteSettings()) {
            return SettingResult.NeedsPermission(
                getWriteSettingsPermissionIntent(),
                "Modify System Settings"
            )
        }
        return try {
            val value = (fraction * 255f).roundToInt().coerceIn(0, 255)
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                value
            )
            SettingResult.Direct("Brightness set to ${(fraction * 100).toInt()}%")
        } catch (e: Exception) {
            SettingResult.Direct("Failed to change brightness: ${e.message}")
        }
    }

    // --- SOUND / VOLUME ---
    fun getMediaVolume(): Float {
        if (audioManager == null) return 0.5f
        return try {
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            if (max > 0) current.toFloat() / max else 0f
        } catch (_: Exception) {
            0.5f
        }
    }

    fun setMediaVolume(fraction: Float): SettingResult {
        if (audioManager == null) return SettingResult.Direct("Audio service unavailable")
        return try {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val volume = (fraction * max).roundToInt().coerceIn(0, max)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
            SettingResult.Direct("Volume set to ${(fraction * 100).toInt()}%")
        } catch (e: Exception) {
            SettingResult.Direct("Volume change error: ${e.message}")
        }
    }

    // --- SCREEN TIMEOUT ---
    fun getScreenTimeoutMs(): Int {
        return try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT
            )
        } catch (_: Exception) {
            60000
        }
    }

    fun setScreenTimeout(ms: Int): SettingResult {
        if (!canWriteSettings()) {
            return SettingResult.NeedsPermission(
                getWriteSettingsPermissionIntent(),
                "Modify System Settings"
            )
        }
        return try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                ms
            )
            SettingResult.Direct("Screen timeout set to ${ms / 1000}s")
        } catch (e: Exception) {
            SettingResult.Direct("Failed: ${e.message}")
        }
    }

    // --- ROTATION LOCK ---
    fun isAutoRotationEnabled(): Boolean {
        return try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION
            ) == 1
        } catch (_: Exception) {
            false
        }
    }

    fun setAutoRotation(enabled: Boolean): SettingResult {
        if (!canWriteSettings()) {
            return SettingResult.NeedsPermission(
                getWriteSettingsPermissionIntent(),
                "Modify System Settings"
            )
        }
        return try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                if (enabled) 1 else 0
            )
            SettingResult.Direct(if (enabled) "Auto-rotation enabled" else "Rotation locked")
        } catch (e: Exception) {
            SettingResult.Direct("Failed: ${e.message}")
        }
    }

    // --- FLASHLIGHT / TORCH ---
    fun toggleTorch(enabled: Boolean): SettingResult {
        if (cameraManager == null) return SettingResult.Direct("Camera service unavailable")
        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, enabled)
                SettingResult.Direct(if (enabled) "Flashlight on" else "Flashlight off")
            } else {
                SettingResult.Direct("Flashlight not available on this device")
            }
        } catch (e: Exception) {
            SettingResult.Direct("Flashlight error: ${e.message}")
        }
    }

    // --- BATTERY STATUS ---
    fun getBatteryLevel(): Int {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
            if (level in 0..100) level else 100
        } catch (_: Exception) {
            100
        }
    }

    // --- DEEP LINKS ---
    fun openWifiSettings(): SettingResult =
        SettingResult.DeepLink(Intent(Settings.ACTION_WIFI_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }, "Wi-Fi Settings")

    fun openBluetoothSettings(): SettingResult =
        SettingResult.DeepLink(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }, "Bluetooth Settings")

    fun openAirplaneModeSettings(): SettingResult =
        SettingResult.DeepLink(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }, "Airplane Mode")

    fun openDisplaySettings(): SettingResult =
        SettingResult.DeepLink(Intent(Settings.ACTION_DISPLAY_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }, "Display Settings")

    fun openSoundSettings(): SettingResult =
        SettingResult.DeepLink(Intent(Settings.ACTION_SOUND_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }, "Sound Settings")

    fun openBatterySettings(): SettingResult =
        SettingResult.DeepLink(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }, "Battery Saver")

    fun openDefaultAppsSettings(): SettingResult =
        SettingResult.DeepLink(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }, "Default Apps")

    fun openNotificationListenerSettings(): SettingResult =
        SettingResult.DeepLink(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }, "Notification Access")

    fun openDndSettings(): SettingResult =
        SettingResult.DeepLink(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }, "Do Not Disturb Access")
}
