package com.fluentshell.app

import android.app.Application
import com.fluentshell.core.data.db.FluentDatabase
import com.fluentshell.core.data.repository.AppRepository
import com.fluentshell.core.data.repository.ThemePreferencesRepository
import com.fluentshell.core.system.SystemSettingsGateway
import com.fluentshell.feature.notifications.NotificationStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class FluentApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: FluentDatabase
        private set

    lateinit var themeRepository: ThemePreferencesRepository
        private set

    lateinit var appRepository: AppRepository
        private set

    lateinit var systemSettingsGateway: SystemSettingsGateway
        private set

    lateinit var notificationStore: NotificationStore
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = try {
            FluentDatabase.getInstance(this)
        } catch (_: Exception) {
            androidx.room.Room.inMemoryDatabaseBuilder(this, FluentDatabase::class.java).build()
        }
        themeRepository = ThemePreferencesRepository(this)
        appRepository = AppRepository(this, database.fluentDao(), applicationScope)
        systemSettingsGateway = SystemSettingsGateway(this)
        notificationStore = NotificationStore(applicationScope)
    }

    companion object {
        lateinit var instance: FluentApplication
            private set
    }
}
