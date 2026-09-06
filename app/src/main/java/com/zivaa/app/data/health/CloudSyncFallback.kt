package com.zivaa.app.data.health

interface CloudSyncFallback {
    /**
     * Executes fallback data syncing mechanisms (e.g. FitBit/Garmin API integrations).
     * Returns true if successfully synced, false otherwise.
     */
    suspend fun triggerCloudToCloudSync(): Boolean
}
