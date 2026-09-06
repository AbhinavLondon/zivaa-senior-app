package com.zivaa.app.data.sensors

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PhoneSensorService : Service() {

    private val CHANNEL_ID = "SensorServiceChannel"
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var dataStore: SensorDataStore
    private lateinit var audioAnalyzer: AudioAnalyzer
    private lateinit var mobilityAnalyzer: MobilityAnalyzer

    override fun onCreate() {
        super.onCreate()
        dataStore = SensorDataStore(this)
        audioAnalyzer = AudioAnalyzer(dataStore)
        mobilityAnalyzer = MobilityAnalyzer(this, dataStore)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Zivaa Health Monitoring")
            .setContentText("Actively monitoring health sensors for insights")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Placeholder icon
            .build()

        startForeground(1, notification)

        // Start collecting data
        serviceScope.launch {
            audioAnalyzer.startListening()
        }
        mobilityAnalyzer.startListening()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        mobilityAnalyzer.stopListening()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Sensor Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }
}
