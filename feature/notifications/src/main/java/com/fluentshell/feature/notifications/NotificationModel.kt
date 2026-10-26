package com.fluentshell.feature.notifications

import android.graphics.drawable.Drawable

data class NotificationModel(
    val id: String,
    val packageName: String,
    val appTitle: String,
    val title: String,
    val text: String,
    val postTime: Long = System.currentTimeMillis(),
    val icon: Drawable? = null
)
