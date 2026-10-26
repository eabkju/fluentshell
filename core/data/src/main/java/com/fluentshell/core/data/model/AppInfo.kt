package com.fluentshell.core.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable? = null,
    val isPinnedToStart: Boolean = false,
    val isPinnedToTaskbar: Boolean = false,
    val installTime: Long = 0L
)
