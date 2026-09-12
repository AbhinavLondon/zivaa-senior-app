package com.zivaa.app.presentation.setup.wearable

import java.time.Instant

data class BrandSyncReport(
    val isAppInstalled: Boolean = false,
    val hasHealthConnectRecords: Boolean = false,
    val latestHeartRateBpm: Int? = null,
    val totalStepsToday: Long? = null,
    val lastSyncTime: Instant? = null
)

sealed class WearableSetupStep {
    object BrandPicker : WearableSetupStep()
    data class CheckingStatus(val brand: WearableBrand) : WearableSetupStep()
    data class AlreadyConnected(val brand: WearableBrand, val bpm: Int?, val steps: Long?) : WearableSetupStep()
    data class BridgeRequired(val brand: WearableBrand) : WearableSetupStep()
    data class CompanionPairing(val brand: WearableBrand, val isAppInstalled: Boolean) : WearableSetupStep()
    data class WearItRight(val brand: WearableBrand) : WearableSetupStep()
}
