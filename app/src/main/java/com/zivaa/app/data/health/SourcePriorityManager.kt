package com.zivaa.app.data.health

import androidx.health.connect.client.records.StepsRecord
import com.zivaa.app.data.local.SyncPrefsManager
import com.zivaa.app.data.remote.MetricSourcePriorityRecord

class SourcePriorityManager(private val syncPrefsManager: SyncPrefsManager) {

    fun getPriority(metricType: String, source: String): Int {
        val priorities = syncPrefsManager.getSourcePriorities()
        return getPriority(metricType, source, priorities)
    }

    fun resolveSteps(records: List<StepsRecord>): Long {
        val priorities = syncPrefsManager.getSourcePriorities()
        return resolveSteps(records, priorities)
    }

    companion object {
        fun getPriority(
            metricType: String,
            source: String,
            priorities: List<MetricSourcePriorityRecord>
        ): Int {
            val match = priorities.firstOrNull { it.metric_type == metricType && it.source == source }
            if (match != null) return match.priority_rank

            return when {
                source.endsWith("_watch") -> 5
                source.endsWith("_phone") -> 50
                else -> 9999
            }
        }

        fun resolveSteps(
            records: List<StepsRecord>,
            priorities: List<MetricSourcePriorityRecord> = emptyList()
        ): Long {
            if (records.isEmpty()) return 0L

            // Group records by computed source tag (matching Postgres ingestion tag)
            val sourceSums = records.groupBy { record ->
                val meta = record.metadata
                var src = meta.dataOrigin.packageName
                val deviceType = meta.device?.type
                if (deviceType == 1) { // TYPE_WATCH
                    src += "_watch"
                } else if (deviceType == 2) { // TYPE_PHONE
                    src += "_phone"
                }
                src
            }.mapValues { (_, recs) -> recs.sumOf { it.count } }

            // Find the winning source with the lowest priority rank
            val winningSource = sourceSums.keys.minByOrNull { source ->
                getPriority("StepsRecord", source, priorities)
            }

            val winningSourceSteps = winningSource?.let { sourceSums[it] } ?: 0L
            val phoneSteps = sourceSums.filterKeys { it.endsWith("_phone") }.values.maxOrNull() ?: 0L

            // Fallback rule matching Postgres: GREATEST(o.total_steps, spf.phone_steps)
            return maxOf(winningSourceSteps, phoneSteps)
        }
    }
}

