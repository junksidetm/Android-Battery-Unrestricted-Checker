package com.unrestricted.batterychecker.service

import com.unrestricted.batterychecker.model.BatteryOptimizationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryStateParserTest {

    @Test
    fun `parses modern whitelist and skips except-idle entries`() {
        val output = """
            system-excidle,com.android.providers.downloads,10037
            system,com.google.android.gms,10150
            user,com.whatsapp,10180
        """.trimIndent()

        assertEquals(
            setOf("com.google.android.gms", "com.whatsapp"),
            BatteryStateParser.parseWhitelist(output)
        )
    }

    @Test
    fun `parses legacy colon whitelist and ignores bare uids`() {
        val output = "system:com.android.phone\nuser:org.telegram.messenger\n10234\n"

        assertEquals(
            setOf("com.android.phone", "org.telegram.messenger"),
            BatteryStateParser.parseWhitelist(output)
        )
    }

    @Test
    fun `parses appops package list and ignores status text`() {
        val output = "com.facebook.katana\n  com.example.game  \nNo operations.\n"

        assertEquals(
            setOf("com.facebook.katana", "com.example.game"),
            BatteryStateParser.parsePackageList(output)
        )
    }

    @Test
    fun `whitelist takes precedence over restricted and default is optimized`() {
        val whitelist = setOf("com.app.whitelisted")
        val restricted = setOf("com.app.restricted")

        // Whitelisted app is Unrestricted
        assertEquals(
            BatteryOptimizationState.UNRESTRICTED,
            BatteryStateParser.classify("com.app.whitelisted", whitelist, restricted)
        )

        // Restricted app is Restricted
        assertEquals(
            BatteryOptimizationState.RESTRICTED,
            BatteryStateParser.classify("com.app.restricted", whitelist, restricted)
        )

        // App in neither is Optimized
        assertEquals(
            BatteryOptimizationState.OPTIMIZED,
            BatteryStateParser.classify("com.app.normal", whitelist, restricted)
        )

        // If an app appears in both, Whitelist wins (v3.2.0 Decoupling Architecture Mandate)
        val conflicting = setOf("com.app.conflict")
        assertEquals(
            BatteryOptimizationState.UNRESTRICTED,
            BatteryStateParser.classify("com.app.conflict", conflicting, conflicting)
        )
    }

    @Test
    fun `validates package name and rejects command injection`() {
        assertTrue(BatteryStateParser.isPackageName("com.whatsapp"))
        assertTrue(BatteryStateParser.isPackageName("org.telegram.messenger"))
        assertFalse(BatteryStateParser.isPackageName("com.whatsapp;reboot"))
        assertFalse(BatteryStateParser.isPackageName("com.app/"))
        assertFalse(BatteryStateParser.isPackageName("bareword"))

        assertThrows(IllegalArgumentException::class.java) {
            BatteryStateParser.switchCommand("com.whatsapp;reboot", BatteryOptimizationState.UNRESTRICTED)
        }
    }

    @Test
    fun `generates batched transactional shell commands`() {
        val unrestrictedCmd = BatteryStateParser.switchCommand("com.test.app", BatteryOptimizationState.UNRESTRICTED)
        assertEquals(
            "dumpsys deviceidle whitelist +com.test.app; cmd appops set com.test.app RUN_ANY_IN_BACKGROUND allow; cmd appops set com.test.app RUN_IN_BACKGROUND allow; am set-standby-bucket com.test.app active",
            unrestrictedCmd
        )

        val restrictedCmd = BatteryStateParser.switchCommand("com.test.app", BatteryOptimizationState.RESTRICTED)
        assertEquals(
            "dumpsys deviceidle whitelist -com.test.app; cmd appops set com.test.app RUN_ANY_IN_BACKGROUND ignore; cmd appops set com.test.app RUN_IN_BACKGROUND ignore; am set-standby-bucket com.test.app restricted",
            restrictedCmd
        )
    }
}
