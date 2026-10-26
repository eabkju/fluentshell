package com.fluentshell.feature.notifications

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationStore(private val scope: CoroutineScope) {

    private val _notifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notifications: StateFlow<List<NotificationModel>> = _notifications.asStateFlow()

    private val _currentToast = MutableStateFlow<NotificationModel?>(null)
    val currentToast: StateFlow<NotificationModel?> = _currentToast.asStateFlow()

    private var toastDismissJob: Job? = null

    fun postNotification(notification: NotificationModel) {
        val current = _notifications.value.filter { it.id != notification.id }.toMutableList()
        current.add(0, notification)
        _notifications.value = current

        // Trigger Toast popup
        _currentToast.value = notification
        toastDismissJob?.cancel()
        toastDismissJob = scope.launch {
            delay(5000)
            if (_currentToast.value?.id == notification.id) {
                _currentToast.value = null
            }
        }
    }

    fun dismissNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
        if (_currentToast.value?.id == id) {
            _currentToast.value = null
        }
    }

    fun dismissToast() {
        _currentToast.value = null
        toastDismissJob?.cancel()
    }

    fun clearAll() {
        _notifications.value = emptyList()
        _currentToast.value = null
        toastDismissJob?.cancel()
    }
}
