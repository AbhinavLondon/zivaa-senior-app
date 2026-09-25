package com.zivaa.app.data.telemetry

import com.google.gson.annotations.SerializedName

/**
 * SupabaseScreenViewRecord
 * Matches public.telemetry_screen_views table schema.
 */
data class SupabaseScreenViewRecord(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("screen_id")
    val screenId: String,

    @SerializedName("screen_name")
    val screenName: String,

    @SerializedName("entered_at")
    val enteredAt: String,

    @SerializedName("exited_at")
    val exitedAt: String? = null,

    @SerializedName("dwell_time_ms")
    val dwellTimeMs: Long,

    @SerializedName("is_bounce")
    val isBounce: Boolean = false,

    @SerializedName("action_count")
    val actionCount: Int = 0,

    @SerializedName("session_id")
    val sessionId: String? = null
)

/**
 * SupabaseTelemetryEventRecord
 * Matches public.telemetry_events table schema.
 */
data class SupabaseTelemetryEventRecord(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("screen_id")
    val screenId: String,

    @SerializedName("target_id")
    val targetId: String,

    @SerializedName("target_label")
    val targetLabel: String? = null,

    @SerializedName("event_type")
    val eventType: String, // "USER_ACTION", "RAGE_TAP", "DEAD_TAP"

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("metadata")
    val metadata: Map<String, Any>? = null,

    @SerializedName("session_id")
    val sessionId: String? = null
)
