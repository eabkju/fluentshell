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

@Entity(tableName = "desktop_items")
data class DesktopItemEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val packageName: String = "",
    val activityName: String = "",
    val isFolder: Boolean = false,
    val folderItems: String = "", // Comma-separated package names
    val posX: Float = 0f,
    val posY: Float = 0f
)
