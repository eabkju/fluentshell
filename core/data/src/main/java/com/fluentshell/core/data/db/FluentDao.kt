package com.fluentshell.core.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FluentDao {
    @Query("SELECT * FROM pinned_apps WHERE pinnedLocation = :location ORDER BY orderIndex ASC")
    fun getPinnedApps(location: String): Flow<List<PinnedAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPinnedApp(app: PinnedAppEntity)

    @Query("DELETE FROM pinned_apps WHERE packageName = :packageName AND pinnedLocation = :location")
    suspend fun removePinnedApp(packageName: String, location: String)

    @Query("SELECT * FROM desktop_icons")
    fun getDesktopIcons(): Flow<List<DesktopIconEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDesktopIcon(icon: DesktopIconEntity)

    @Delete
    suspend fun deleteDesktopIcon(icon: DesktopIconEntity)

    @Query("DELETE FROM desktop_icons WHERE packageName = :packageName")
    suspend fun deleteDesktopIconsByPackage(packageName: String)
}
