package com.zivaa.app.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

class MobilityAnalyzer(context: Context, private val dataStore: SensorDataStore) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private var isRegistered = false
    
    // Heuristic state for Sit-to-Stand
    private var isStandingUp = false
    private var standingStartTime = 0L

    fun startListening() {
        if (!isRegistered && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            isRegistered = true
        }
    }

    fun stopListening() {
        if (isRegistered) {
            sensorManager.unregisterListener(this)
            isRegistered = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            // Calculate total acceleration magnitude
            val magnitude = sqrt((x * x + y * y + z * z).toDouble())
            
            // HEURISTIC: A simple threshold-based heuristic for sit-to-stand.
            // When standing up, there's a strong acceleration pulse, followed by a settling.
            // In a real app, you would use a trained model or complex peak detection.
            
            // Assume 9.8 is gravity. A spike above 12.0 could be a motion initiation.
            if (magnitude > 12.0 && !isStandingUp) {
                isStandingUp = true
                standingStartTime = System.currentTimeMillis()
            } else if (magnitude in 9.5..10.5 && isStandingUp) {
                // Stabilized
                val durationMs = System.currentTimeMillis() - standingStartTime
                // Plausible sit-to-stand duration is between 1.0s and 5.0s
                if (durationMs in 1000..5000) {
                    dataStore.addSitToStandTime(durationMs / 1000.0)
                }
                isStandingUp = false
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}
