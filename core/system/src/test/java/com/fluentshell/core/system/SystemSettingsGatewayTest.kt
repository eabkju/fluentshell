package com.fluentshell.core.system

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemSettingsGatewayTest {

    @Test
    fun testSettingResultDirect() {
        val direct = SettingResult.Direct("Volume changed")
        assertTrue(direct is SettingResult)
        assertNotNull(direct.message)
    }

    @Test
    fun testSettingResultDeepLink() {
        val link = SettingResult.DeepLink(android.content.Intent(), "Wi-Fi Settings")
        assertTrue(link is SettingResult)
        org.junit.Assert.assertEquals("Wi-Fi Settings", link.description)
    }
}
