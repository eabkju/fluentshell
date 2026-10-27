package com.fluentshell.core.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Process
import com.fluentshell.core.data.db.FluentDao
import com.fluentshell.core.data.db.PinnedAppEntity
import com.fluentshell.core.data.model.AppInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val fluentDao: FluentDao,
    private val scope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val _rawApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

    init {
        scope.launch {
            combine(
                _rawApps,
                fluentDao.getPinnedApps("START"),
                fluentDao.getPinnedApps("TASKBAR")
            ) { apps, pinnedStart, pinnedTaskbar ->
                val startSet = pinnedStart.map { it.packageName }.toSet()
                val taskbarSet = pinnedTaskbar.map { it.packageName }.toSet()

                apps.map { app ->
                    app.copy(
                        isPinnedToStart = startSet.contains(app.packageName),
                        isPinnedToTaskbar = taskbarSet.contains(app.packageName)
                    )
                }
            }.collect { merged ->
                _allApps.value = merged
            }
        }
        refreshApps()
    }

    fun refreshApps() {
        scope.launch(ioDispatcher) {
            try {
                val list = queryInstalledApps()
                _rawApps.value = list
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun queryInstalledApps(): List<AppInfo> = withContext(ioDispatcher) {
        val apps = mutableListOf<AppInfo>()
        val seenPackages = mutableSetOf<String>()

        // 1. Try standard PackageManager queryIntentActivities with flag 0
        try {
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = context.packageManager.queryIntentActivities(intent, 0)
            for (info in resolveInfos) {
                val pkgName = info.activityInfo?.packageName ?: continue
                if (pkgName == context.packageName || seenPackages.contains(pkgName)) continue
                seenPackages.add(pkgName)

                val label = try {
                    info.loadLabel(context.packageManager)?.toString() ?: pkgName
                } catch (_: Exception) {
                    pkgName
                }
                val icon = try {
                    info.loadIcon(context.packageManager)
                } catch (_: Exception) {
                    null
                }

                apps.add(
                    AppInfo(
                        packageName = pkgName,
                        activityName = info.activityInfo.name,
                        label = label,
                        icon = icon
                    )
                )
            }
        } catch (_: Exception) {
        }

        // 2. If empty, fallback to LauncherApps
        if (apps.isEmpty()) {
            try {
                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
                val userHandle = Process.myUserHandle()
                if (launcherApps != null) {
                    val activities = launcherApps.getActivityList(null, userHandle)
                    for (activity in activities) {
                        val pkgName = activity.applicationInfo.packageName
                        if (pkgName == context.packageName || seenPackages.contains(pkgName)) continue
                        seenPackages.add(pkgName)

                        val label = activity.label?.toString() ?: pkgName
                        val icon = try {
                            activity.getBadgedIcon(0)
                        } catch (_: Exception) {
                            null
                        }

                        apps.add(
                            AppInfo(
                                packageName = pkgName,
                                activityName = activity.name,
                                label = label,
                                icon = icon
                            )
                        )
                    }
                }
            } catch (_: Exception) {
            }
        }

        apps.sortedBy { it.label.lowercase() }
    }

    suspend fun pinAppToStart(app: AppInfo) = withContext(ioDispatcher) {
        fluentDao.insertPinnedApp(
            PinnedAppEntity(
                packageName = app.packageName,
                activityName = app.activityName,
                label = app.label,
                pinnedLocation = "START",
                orderIndex = 0
            )
        )
    }

    suspend fun unpinAppFromStart(packageName: String) = withContext(ioDispatcher) {
        fluentDao.removePinnedApp(packageName, "START")
    }

    suspend fun pinAppToTaskbar(app: AppInfo) = withContext(ioDispatcher) {
        fluentDao.insertPinnedApp(
            PinnedAppEntity(
                packageName = app.packageName,
                activityName = app.activityName,
                label = app.label,
                pinnedLocation = "TASKBAR",
                orderIndex = 0
            )
        )
    }

    suspend fun unpinAppFromTaskbar(packageName: String) = withContext(ioDispatcher) {
        fluentDao.removePinnedApp(packageName, "TASKBAR")
    }

    fun searchApps(query: String): List<AppInfo> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return _allApps.value
        return _allApps.value.filter {
            it.label.lowercase().contains(trimmed) || it.packageName.lowercase().contains(trimmed)
        }
    }
}
