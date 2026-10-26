package com.fluentshell.app

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.fluentshell.feature.notifications.NotificationModel

class FluentNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        val pkgName = sbn.packageName
        if (pkgName == packageName) return // Do not mirror our own notifications

        val appTitle = try {
            val appInfo = packageManager.getApplicationInfo(pkgName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            pkgName
        }

        val icon = try {
            packageManager.getApplicationIcon(pkgName)
        } catch (_: Exception) {
            null
        }

        val model = NotificationModel(
            id = sbn.key,
            packageName = pkgName,
            appTitle = appTitle,
            title = title,
            text = text,
            postTime = sbn.postTime,
            icon = icon
        )

        FluentApplication.instance.notificationStore.postNotification(model)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null) return
        FluentApplication.instance.notificationStore.dismissNotification(sbn.key)
    }
}
