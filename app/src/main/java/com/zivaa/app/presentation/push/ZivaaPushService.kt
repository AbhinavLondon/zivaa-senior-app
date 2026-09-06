package com.zivaa.app.presentation.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ZivaaPushService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["nudge_title"] ?: "Zivaa Eldercare Updates"
        val body = remoteMessage.data["nudge_text"] ?: "Check vitals on the dashboard."
        val riskLevel = remoteMessage.data["risk_level"] ?: "LOW"

        showSystemNotification(title, body, riskLevel)
    }

    private fun showSystemNotification(title: String, message: String, risk: String) {
        val channelId = "zivaa_early_warnings"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Early Warning Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Empathetic plain-language clinical warning nudges."
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Color coding mapped to Zivaa design system styling
        val color = when (risk) {
            "HIGH" -> 0xA4493D   // Zivaa Rose (Alerts/Discontinued)
            "MEDIUM" -> 0xC98A3A // Zivaa Amber (Honey / Caution)
            else -> 0x234B3F     // Zivaa Sage (Forest Green)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setColor(color)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
