package com.fluentshell.feature.notifications

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationStoreTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun testPostNotification() = testScope.runTest {
        val store = NotificationStore(this)
        val notif = NotificationModel(
            id = "notif_1",
            packageName = "com.test.app",
            appTitle = "Test App",
            title = "Hello",
            text = "World"
        )

        store.postNotification(notif)
        assertEquals(1, store.notifications.value.size)
        assertEquals(notif, store.currentToast.value)
    }

    @Test
    fun testDismissNotification() = testScope.runTest {
        val store = NotificationStore(this)
        val notif = NotificationModel(
            id = "notif_1",
            packageName = "com.test.app",
            appTitle = "Test App",
            title = "Hello",
            text = "World"
        )

        store.postNotification(notif)
        store.dismissNotification("notif_1")
        assertEquals(0, store.notifications.value.size)
        assertNull(store.currentToast.value)
    }

    @Test
    fun testClearAllNotifications() = testScope.runTest {
        val store = NotificationStore(this)
        store.postNotification(NotificationModel(id = "1", packageName = "a", appTitle = "A", title = "1", text = "1"))
        store.postNotification(NotificationModel(id = "2", packageName = "b", appTitle = "B", title = "2", text = "2"))

        assertEquals(2, store.notifications.value.size)
        store.clearAll()
        assertEquals(0, store.notifications.value.size)
    }
}
