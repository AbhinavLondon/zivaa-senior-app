package com.zivaa.app.data.health.worker

import kotlinx.coroutines.sync.Mutex

/**
 * Shared synchronization coordinator to prevent concurrent execution collisions
 * between live delta sync (HealthDataSyncWorker) and deep historical sync (HealthHistoricalBackfillWorker).
 */
object HealthSyncCoordinator {
    val syncMutex = Mutex()
}
