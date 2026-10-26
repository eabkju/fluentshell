package com.fluentshell.core.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PinnedAppEntity::class, DesktopIconEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FluentDatabase : RoomDatabase() {
    abstract fun fluentDao(): FluentDao

    companion object {
        @Volatile
        private var INSTANCE: FluentDatabase? = null

        fun getInstance(context: Context): FluentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FluentDatabase::class.java,
                    "fluentshell.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
