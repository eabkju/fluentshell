package com.fluentshell.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pinned_apps")
data class PinnedAppEntity(
    @PrimaryKey
    val packageName: String,
    val activityName: String,
    val label: String,
    val pinnedLocation: String, // "START" or "TASKBAR"
    val orderIndex: Int
)

@Entity(tableName = "desktop_icons")
data class DesktopIconEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val activityName: String,
    val label: String,
    val gridX: Int,
    val gridY: Int
)
