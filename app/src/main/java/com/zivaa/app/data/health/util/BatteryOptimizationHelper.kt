package com.zivaa.app.data.health.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

/**
 * Industry-standard helper for checking and guiding Android Battery Optimization settings.
 * 100% Google Play compliant: does not require high-risk manifest permissions.
 */
object BatteryOptimizationHelper {

    /**
     * Returns true if the app has been granted "Unrestricted" battery usage.
     * Returns false if the app is in "Optimized" or "Restricted" mode (where Android Doze / Adaptive Battery
     * may defer or kill background synchronization).
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Safely directs the user to the app's battery settings screen.
     * Tries direct App Details settings first (which shows "App battery usage"),
     * with graceful fallback to system battery optimization list.
     */
    fun openBatterySettings(context: Context) {
        try {
            // Direct navigation to Zivaa's App Info page where "App battery usage" is prominent
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                // Fallback: system-wide battery optimization list
                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (ignored: Exception) {
                try {
                    val generalSettings = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(generalSettings)
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
        }
    }
}
