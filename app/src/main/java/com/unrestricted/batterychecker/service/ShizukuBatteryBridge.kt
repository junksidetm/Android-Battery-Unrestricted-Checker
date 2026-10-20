package com.unrestricted.batterychecker.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.unrestricted.batterychecker.model.BatteryOptimizationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

object ShizukuBatteryBridge {
    private const val TAG = "ShizukuBatteryBridge"

    /**
     * Checks if Shizuku server is alive and responding.
     */
    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Checks if this app has permission to invoke Shizuku commands.
     */
    fun hasPermission(): Boolean {
        return try {
            if (!isShizukuAvailable()) return false
            if (Shizuku.isPreV11()) return false
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Requests Shizuku permission.
     */
    fun requestPermission(requestCode: Int = 1001) {
        try {
            if (isShizukuAvailable() && !Shizuku.isPreV11()) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to request Shizuku permission", e)
        }
    }

    /**
     * Invokes Shizuku remote process via reflection to maintain compatibility with modern Shizuku API versions.
     */
    private fun execShizukuCommand(command: Array<String>): String {
        return try {
            val clazz = Class.forName("rikka.shizuku.Shizuku")
            val method = clazz.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, command, null, null) as Process
            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            process.destroy()
            output.trim()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to execute Shizuku command: ${command.joinToString(" ")}", e)
            ""
        }
    }

    /**
     * Fetches the complete DeviceIdle (Doze) whitelist by querying dumpsys via Shizuku.
     */
    suspend fun getShizukuPowerWhitelist(): Set<String> = withContext(Dispatchers.IO) {
        val whitelisted = mutableSetOf<String>()
        if (!hasPermission()) return@withContext whitelisted

        val output = execShizukuCommand(arrayOf("dumpsys", "deviceidle", "whitelist"))
        output.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.contains(",")) {
                val parts = trimmed.split(",")
                if (parts.isNotEmpty()) {
                    whitelisted.add(parts[0].trim())
                }
            } else if (trimmed.startsWith("system:") || trimmed.startsWith("user:")) {
                val pkg = trimmed.substringAfter(":").trim()
                if (pkg.isNotEmpty()) {
                    whitelisted.add(pkg)
                }
            }
        }
        whitelisted
    }

    /**
     * Queries all packages having RUN_ANY_IN_BACKGROUND or RUN_IN_BACKGROUND ignored (Restricted).
     */
    suspend fun getRestrictedPackages(): Set<String> = withContext(Dispatchers.IO) {
        val restricted = mutableSetOf<String>()
        if (!hasPermission()) return@withContext restricted

        // 1. Query RUN_ANY_IN_BACKGROUND
        val outputAny = execShizukuCommand(
            arrayOf("cmd", "appops", "query-op", "--user", "0", "RUN_ANY_IN_BACKGROUND", "ignore")
        )
        parseOpOutput(outputAny, restricted)

        // 2. Query RUN_IN_BACKGROUND
        val outputRun = execShizukuCommand(
            arrayOf("cmd", "appops", "query-op", "--user", "0", "RUN_IN_BACKGROUND", "ignore")
        )
        parseOpOutput(outputRun, restricted)

        restricted
    }

    private fun parseOpOutput(output: String, targetSet: MutableSet<String>) {
        output.lineSequence().forEach { line ->
            val trimmed = line.trim()
            val pkg = trimmed.substringBefore(":").substringBefore(" ").trim()
            if (pkg.isNotEmpty() && pkg.contains(".")) {
                targetSet.add(pkg)
            }
        }
    }

    /**
     * Evaluates the battery state for a package given whitelist and restricted sets.
     */
    fun evaluateState(
        packageName: String,
        isWhitelisted: Boolean,
        isRestricted: Boolean,
        shizukuActive: Boolean
    ): BatteryOptimizationState {
        return if (shizukuActive) {
            when {
                isWhitelisted -> BatteryOptimizationState.UNRESTRICTED
                isRestricted -> BatteryOptimizationState.RESTRICTED
                else -> BatteryOptimizationState.OPTIMIZED
            }
        } else {
            // Non-Shizuku fallback: standard public API
            if (isWhitelisted) {
                BatteryOptimizationState.UNRESTRICTED
            } else {
                BatteryOptimizationState.OPTIMIZED
            }
        }
    }

    /**
     * Changes an app's optimization mode directly using Shizuku with Standby Bucket and AppOps synchronization.
     */
    suspend fun setOptimizationMode(
        packageName: String,
        targetState: BatteryOptimizationState
    ): Boolean = withContext(Dispatchers.IO) {
        if (!hasPermission()) return@withContext false

        try {
            when (targetState) {
                BatteryOptimizationState.UNRESTRICTED -> {
                    // Doze whitelist + AppOps Allow + Standby Bucket ACTIVE
                    execShizukuCommand(arrayOf("dumpsys", "deviceidle", "whitelist", "+$packageName"))
                    execShizukuCommand(arrayOf("cmd", "appops", "set", packageName, "RUN_ANY_IN_BACKGROUND", "allow"))
                    execShizukuCommand(arrayOf("cmd", "appops", "set", packageName, "RUN_IN_BACKGROUND", "allow"))
                    execShizukuCommand(arrayOf("am", "set-standby-bucket", packageName, "active"))
                    true
                }
                BatteryOptimizationState.OPTIMIZED -> {
                    // Remove Doze whitelist + AppOps Allow + Standby Bucket WORKING_SET
                    execShizukuCommand(arrayOf("dumpsys", "deviceidle", "whitelist", "-$packageName"))
                    execShizukuCommand(arrayOf("cmd", "appops", "set", packageName, "RUN_ANY_IN_BACKGROUND", "allow"))
                    execShizukuCommand(arrayOf("cmd", "appops", "set", packageName, "RUN_IN_BACKGROUND", "allow"))
                    execShizukuCommand(arrayOf("am", "set-standby-bucket", packageName, "working_set"))
                    true
                }
                BatteryOptimizationState.RESTRICTED -> {
                    // Remove Doze whitelist + AppOps Ignore + Standby Bucket RESTRICTED
                    execShizukuCommand(arrayOf("dumpsys", "deviceidle", "whitelist", "-$packageName"))
                    execShizukuCommand(arrayOf("cmd", "appops", "set", packageName, "RUN_ANY_IN_BACKGROUND", "ignore"))
                    execShizukuCommand(arrayOf("cmd", "appops", "set", packageName, "RUN_IN_BACKGROUND", "ignore"))
                    execShizukuCommand(arrayOf("am", "set-standby-bucket", packageName, "restricted"))
                    true
                }
                BatteryOptimizationState.UNKNOWN -> false
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to set optimization mode for $packageName", e)
            false
        }
    }

    /**
     * Opens native system battery settings using OEM intent cascade.
     */
    fun openNativeBatterySettings(context: Context) {
        val candidates = listOf(
            // Realme / ColorOS / Oppo
            ComponentName("com.oplus.battery", "com.oplus.battery.settings.AppBatteryManagerActivity"),
            ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"),
            // MIUI / HyperOS
            ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings"),
            // Samsung One UI
            ComponentName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity"),
            ComponentName("com.samsung.android.sm", "com.samsung.android.sm.battery.ui.BatteryActivity")
        )

        for (comp in candidates) {
            try {
                val intent = Intent().apply {
                    component = comp
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()) {
                    context.startActivity(intent)
                    return
                }
            } catch (e: Throwable) {
                // Ignore and try next
            }
        }

        // Standard AOSP Battery Optimization fallback
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Throwable) {
            try {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            } catch (ignored: Throwable) {}
        }
    }

    /**
     * Fallback method using standard PowerManager.
     */
    fun checkPublicWhitelist(context: Context, packageName: String): Boolean {
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(packageName) == true
        } catch (e: Throwable) {
            false
        }
    }
}
