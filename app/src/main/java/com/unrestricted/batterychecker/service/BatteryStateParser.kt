package com.unrestricted.batterychecker.service

import com.unrestricted.batterychecker.model.BatteryOptimizationState

object BatteryStateParser {
    const val WHITELIST_QUERY = "dumpsys deviceidle whitelist"

    // Settings' "Restricted" sets RUN_ANY_IN_BACKGROUND; older tools set RUN_IN_BACKGROUND.
    const val RESTRICTED_QUERY = "cmd appops query-op --user 0 RUN_ANY_IN_BACKGROUND ignore; " +
        "cmd appops query-op --user 0 RUN_IN_BACKGROUND ignore"

    private val packageNameRegex = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+")

    fun isPackageName(value: String): Boolean = packageNameRegex.matches(value)

    /**
     * Lines look like `system,com.pkg,10150` / `user,com.pkg,10180` (older builds: `system:com.pkg`).
     * `system-excidle` entries are only exempt from app standby, not Doze, so they are not Unrestricted.
     */
    fun parseWhitelist(output: String): Set<String> = output.lineSequence()
        .map { it.trim() }
        .filterNot { it.startsWith("system-excidle") }
        .flatMap { it.split(',', ':', ' ', '\t') }
        .filter(::isPackageName)
        .toSet()

    /** `cmd appops query-op` prints one package per line, or "No operations.". */
    fun parsePackageList(output: String): Set<String> = output.lineSequence()
        .map { it.trim() }
        .filter(::isPackageName)
        .toSet()

    /**
     * Classifies an application's battery optimization state.
     * Crucial Architecture Rule: Whitelist exemption (user Unrestricted battery mode)
     * takes precedence over AppOps, ensuring boot autostart toggles never cause false restrictions.
     */
    fun classify(
        pkg: String,
        whitelist: Set<String>,
        restricted: Set<String>
    ): BatteryOptimizationState {
        if (pkg in whitelist) return BatteryOptimizationState.UNRESTRICTED
        if (pkg in restricted) return BatteryOptimizationState.RESTRICTED
        return BatteryOptimizationState.OPTIMIZED
    }

    /** Generates one batched shell transaction that moves [pkg] into [targetState]. */
    fun switchCommand(pkg: String, targetState: BatteryOptimizationState): String {
        require(isPackageName(pkg)) { "Invalid package name: $pkg" }
        val (whitelistOp, appOpMode, standbyBucket) = when (targetState) {
            BatteryOptimizationState.UNRESTRICTED -> Triple("+", "allow", "active")
            BatteryOptimizationState.OPTIMIZED -> Triple("-", "allow", "working_set")
            BatteryOptimizationState.RESTRICTED -> Triple("-", "ignore", "restricted")
        }
        return listOf(
            "dumpsys deviceidle whitelist $whitelistOp$pkg",
            "cmd appops set $pkg RUN_ANY_IN_BACKGROUND $appOpMode",
            "cmd appops set $pkg RUN_IN_BACKGROUND $appOpMode",
            "am set-standby-bucket $pkg $standbyBucket"
        ).joinToString("; ")
    }
}
