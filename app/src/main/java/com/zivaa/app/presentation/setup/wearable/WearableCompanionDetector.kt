package com.zivaa.app.presentation.setup.wearable

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

object WearableCompanionDetector {

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        if (packageName.isBlank()) return false
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun openAppOrPlayStore(context: Context, packageName: String) {
        if (packageName.isBlank()) return
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(launchIntent)
                return
            }
            val resolveIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                `package` = packageName
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val resolveList = context.packageManager.queryIntentActivities(resolveIntent, 0)
            if (resolveList.isNotEmpty()) {
                val act = resolveList[0].activityInfo
                val explicitIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setClassName(act.packageName, act.name)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(explicitIntent)
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        openPlayStore(context, packageName)
    }

    fun openPlayStore(context: Context, packageName: String) {
        if (packageName.isBlank()) return
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    fun openHealthConnectSettings(context: Context) {
        try {
            val intent = Intent(androidx.health.connect.client.HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open Health Connect settings.", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareCaregiverHelp(context: Context, brand: WearableBrand, extraNote: String = "") {
        val message = buildString {
            append("Hi! I am setting up my ${brand.displayName} on the Zivaa Health app and could use a little help.\n\n")
            if (extraNote.isNotBlank()) {
                append("$extraNote\n\n")
            }
            append("Could you help me next time we speak?")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(sendIntent, "Ask a Family Member for Help").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
