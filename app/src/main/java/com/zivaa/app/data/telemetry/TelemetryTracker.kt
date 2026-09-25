package com.zivaa.app.data.telemetry

import android.util.Log
import com.zivaa.app.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * TelemetryTracker
 *
 * Lightweight, asynchronous singleton that tracks senior app engagement:
 * - Screen entry, exit, and dwell times (in milliseconds)
 * - Bounce detection (exited in <3s with 0 actions)
 * - Micro-actions, rage-tap incidents, and dead taps
 *
 * Zero UI thread overhead: all network dispatches run on Dispatchers.IO.
 */
object TelemetryTracker {
    private const val TAG = "TelemetryTracker"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var activeScreenId: String? = null

    @Volatile
    private var activeScreenName: String? = null

    @Volatile
    private var screenEnteredAtMs: Long = 0L

    @Volatile
    private var screenActionCount: Int = 0

    /**
     * Called whenever the user navigates between screens in Jetpack Compose.
     */
    fun onScreenChanged(newScreenId: String, newScreenName: String, userId: String?) {
        if (newScreenId == activeScreenId) return

        val previousScreenId = activeScreenId
        val previousScreenName = activeScreenName
        val previousEnteredAt = screenEnteredAtMs
        val previousActions = screenActionCount

        // 1. Finalize and dispatch previous screen view session if it lasted >= 500ms
        if (previousScreenId != null && previousEnteredAt > 0L) {
            val nowMs = System.currentTimeMillis()
            val dwellMs = nowMs - previousEnteredAt

            if (dwellMs >= 500L && !userId.isNullOrBlank()) {
                val isBounce = dwellMs < 3000L && previousActions == 0

                val record = SupabaseScreenViewRecord(
                    userId = userId,
                    screenId = previousScreenId,
                    screenName = previousScreenName ?: previousScreenId,
                    enteredAt = Instant.ofEpochMilli(previousEnteredAt).toString(),
                    exitedAt = Instant.ofEpochMilli(nowMs).toString(),
                    dwellTimeMs = dwellMs,
                    isBounce = isBounce,
                    actionCount = previousActions
                )

                scope.launch {
                    try {
                        val response = RetrofitClient.apiService.insertScreenView(record)
                        if (response.isSuccessful) {
                            Log.d(TAG, "Screen view logged: $previousScreenId ($dwellMs ms, bounce=$isBounce)")
                        } else {
                            Log.w(TAG, "Failed to log screen view: HTTP ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error uploading screen view telemetry: ${e.message}")
                    }
                }
            }
        }

        // 2. Set new active screen
        activeScreenId = newScreenId
        activeScreenName = newScreenName
        screenEnteredAtMs = System.currentTimeMillis()
        screenActionCount = 0
    }

    /**
     * Log a user tap/click interaction on a specific UI element.
     */
    fun logAction(targetId: String, targetLabel: String? = null, userId: String?) {
        screenActionCount++

        if (userId.isNullOrBlank()) return
        val currentScreen = activeScreenId ?: "app"

        val event = SupabaseTelemetryEventRecord(
            userId = userId,
            screenId = currentScreen,
            targetId = targetId,
            targetLabel = targetLabel ?: targetId,
            eventType = "USER_ACTION",
            timestamp = Instant.now().toString()
        )

        scope.launch {
            try {
                RetrofitClient.apiService.insertTelemetryEvent(event)
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading action event: ${e.message}")
            }
        }
    }

    /**
     * Log a rage tap incident (>= 3 rapid taps in <= 1000ms).
     */
    fun logRageTap(targetId: String, targetLabel: String? = null, tapCount: Int, durationMs: Long, userId: String?) {
        screenActionCount++

        if (userId.isNullOrBlank()) return
        val currentScreen = activeScreenId ?: "app"

        val event = SupabaseTelemetryEventRecord(
            userId = userId,
            screenId = currentScreen,
            targetId = targetId,
            targetLabel = targetLabel ?: targetId,
            eventType = "RAGE_TAP",
            timestamp = Instant.now().toString(),
            metadata = mapOf(
                "tap_count" to tapCount,
                "duration_ms" to durationMs
            )
        )

        scope.launch {
            try {
                val resp = RetrofitClient.apiService.insertTelemetryEvent(event)
                Log.w(TAG, "Logged RAGE_TAP on $targetId: HTTP ${resp.code()}")
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading rage tap event: ${e.message}")
            }
        }
    }

    /**
     * Log a dead tap (user tapped a static non-interactive element expecting an action).
     */
    fun logDeadTap(targetId: String, targetLabel: String? = null, reason: String, userId: String?) {
        if (userId.isNullOrBlank()) return
        val currentScreen = activeScreenId ?: "app"

        val event = SupabaseTelemetryEventRecord(
            userId = userId,
            screenId = currentScreen,
            targetId = targetId,
            targetLabel = targetLabel ?: targetId,
            eventType = "DEAD_TAP",
            timestamp = Instant.now().toString(),
            metadata = mapOf("reason" to reason)
        )

        scope.launch {
            try {
                RetrofitClient.apiService.insertTelemetryEvent(event)
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading dead tap event: ${e.message}")
            }
        }
    }

    /**
     * Flush remaining active screen session when app enters background.
     */
    fun onAppBackground(userId: String?) {
        val screen = activeScreenId
        val entered = screenEnteredAtMs
        if (screen != null && entered > 0L && !userId.isNullOrBlank()) {
            val now = System.currentTimeMillis()
            val dwell = now - entered
            if (dwell >= 500L) {
                val record = SupabaseScreenViewRecord(
                    userId = userId,
                    screenId = screen,
                    screenName = activeScreenName ?: screen,
                    enteredAt = Instant.ofEpochMilli(entered).toString(),
                    exitedAt = Instant.ofEpochMilli(now).toString(),
                    dwellTimeMs = dwell,
                    isBounce = dwell < 3000L && screenActionCount == 0,
                    actionCount = screenActionCount
                )
                scope.launch {
                    try {
                        RetrofitClient.apiService.insertScreenView(record)
                    } catch (ignored: Exception) {}
                }
            }
        }
        activeScreenId = null
        screenEnteredAtMs = 0L
    }
}
