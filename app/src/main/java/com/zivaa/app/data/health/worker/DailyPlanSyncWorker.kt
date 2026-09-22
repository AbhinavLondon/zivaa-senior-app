package com.zivaa.app.data.health.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.zivaa.app.data.local.SyncPrefsManager
import com.zivaa.app.data.remote.DailyPlanSchedule
import com.zivaa.app.data.remote.RetrofitClient

class DailyPlanSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "DailyPlanSyncWorker"
        const val WORK_NAME = "DailyPlanBackgroundSync"

        fun enqueue(context: Context, patientId: String? = null) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val dataBuilder = Data.Builder()
            if (!patientId.isNullOrBlank()) {
                dataBuilder.putString("patient_id", patientId)
            }

            val request = OneTimeWorkRequestBuilder<DailyPlanSyncWorker>()
                .setConstraints(constraints)
                .setInputData(dataBuilder.build())
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
            android.util.Log.d(TAG, "Enqueued DailyPlanSyncWorker (UniqueWork: REPLACE)")
        }
    }

    override suspend fun doWork(): Result {
        val patientId = inputData.getString("patient_id")
            ?: RetrofitClient.authManager?.getUserId()

        val syncPrefs = SyncPrefsManager(applicationContext)
        val pendingSyncs = syncPrefs.getPendingPlanSyncs(patientId)
        val pendingCheckins = syncPrefs.getPendingPatientCheckins(patientId)

        if (pendingSyncs.isEmpty() && pendingCheckins.isEmpty()) {
            android.util.Log.d(TAG, "No pending daily plan or checkin syncs found. Exiting.")
            return Result.success()
        }

        val gson = Gson()
        var hasFailures = false

        if (pendingSyncs.isNotEmpty()) {
            android.util.Log.i(TAG, "Found ${pendingSyncs.size} pending daily plan sync(s) to upload.")
            for ((planId, scheduleJson) in pendingSyncs) {
                try {
                    val schedule = gson.fromJson(scheduleJson, DailyPlanSchedule::class.java)
                    if (schedule == null) {
                        android.util.Log.w(TAG, "Corrupt schedule JSON for planId $planId. Dropping.")
                        syncPrefs.removePendingPlanSync(planId, patientId)
                        continue
                    }

                    android.util.Log.d(TAG, "Syncing pending schedule for planId $planId to Supabase...")
                    val response = RetrofitClient.apiService.updateDailyPlan(
                        idQuery = "eq.$planId",
                        updates = mapOf("schedule" to schedule)
                    )

                    if (response.isSuccessful) {
                        android.util.Log.i(TAG, "Successfully synced daily plan $planId to Supabase.")
                        syncPrefs.removePendingPlanSync(planId, patientId)
                    } else {
                        android.util.Log.e(TAG, "Failed to sync daily plan $planId: HTTP ${response.code()} ${response.errorBody()?.string()}")
                        hasFailures = true
                    }
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Exception syncing daily plan $planId", e)
                    hasFailures = true
                }
            }
        }

        if (pendingCheckins.isNotEmpty()) {
            android.util.Log.i(TAG, "Found ${pendingCheckins.size} pending patient checkin(s) to upload.")
            for (checkinJson in pendingCheckins) {
                try {
                    val checkin = gson.fromJson(checkinJson, com.zivaa.app.data.remote.SupabasePatientCheckin::class.java)
                    if (checkin == null) {
                        android.util.Log.w(TAG, "Corrupt checkin JSON. Dropping.")
                        syncPrefs.removePendingPatientCheckin(checkinJson, patientId)
                        continue
                    }

                    android.util.Log.d(TAG, "Syncing pending checkin for date ${checkin.date} to Supabase...")
                    val response = RetrofitClient.apiService.insertPatientCheckin(checkin)
                    if (response.isSuccessful) {
                        android.util.Log.i(TAG, "Successfully synced pending checkin for date ${checkin.date} to Supabase.")
                        syncPrefs.removePendingPatientCheckin(checkinJson, patientId)
                    } else {
                        android.util.Log.e(TAG, "Failed to sync checkin: HTTP ${response.code()} ${response.errorBody()?.string()}")
                        hasFailures = true
                    }
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Exception syncing pending checkin", e)
                    hasFailures = true
                }
            }
        }

        return if (hasFailures) {
            android.util.Log.w(TAG, "Some daily plan syncs failed; requesting WorkManager retry.")
            Result.retry()
        } else {
            android.util.Log.i(TAG, "All pending daily plan syncs completed successfully.")
            Result.success()
        }
    }
}
