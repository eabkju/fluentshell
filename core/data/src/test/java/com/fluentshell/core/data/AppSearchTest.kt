package com.fluentshell.core.data

import com.fluentshell.core.data.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSearchTest {

    @Test
    fun testAppSearchFiltering() {
        val sampleApps = listOf(
            AppInfo(packageName = "com.google.android.youtube", activityName = "MainActivity", label = "YouTube"),
            AppInfo(packageName = "com.android.chrome", activityName = "Main", label = "Chrome"),
            AppInfo(packageName = "com.android.settings", activityName = "Settings", label = "Settings"),
            AppInfo(packageName = "com.spotify.music", activityName = "MainActivity", label = "Spotify")
        )

        val query = "you"
        val filtered = sampleApps.filter { it.label.lowercase().contains(query.lowercase()) }

        assertEquals(1, filtered.size)
        assertEquals("YouTube", filtered[0].label)
    }

    @Test
    fun testEmptySearchReturnsAll() {
        val sampleApps = listOf(
            AppInfo(packageName = "com.google.android.youtube", activityName = "MainActivity", label = "YouTube"),
            AppInfo(packageName = "com.android.chrome", activityName = "Main", label = "Chrome")
        )

        val query = ""
        val filtered = if (query.isEmpty()) sampleApps else sampleApps.filter { it.label.contains(query) }
        assertEquals(2, filtered.size)
    }
}
