package com.zivaa.app.data.remote

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.Serializable

@Serializable
data class MorningBriefingEvent(
    val id: String,
    val date: String,
    val summary: String,
    val headline: String? = null,
    val created_at: String
)

object SupabaseRealtimeClient {
    private const val BASE_URL = "https://ecwueqoxfjcepktubqrs.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVjd3VlcW94ZmpjZXBrdHVicXJzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODIyMzcxOTIsImV4cCI6MjA5NzgxMzE5Mn0.2i8tPlBlNKZO1rCo2KFMXG78iZyt40EGm6_R5koDJ08"

    private val supabase: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Realtime)
        }
    }

    suspend fun subscribeToMorningBriefings(patientId: String): Flow<MorningBriefingEvent> {
        val channel = supabase.channel("public:daily_morning_briefings:patient_id=eq.$patientId")
        val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(
            schema = "public"
        ) {
            table = "daily_morning_briefings"
            // Client-side filtering is sufficient since the channel name is already specific to the patient
        }

        channel.subscribe(blockUntilSubscribed = true)
        Log.i("SupabaseRealtime", "Subscribed to realtime morning briefings for patient $patientId")

        return changeFlow.mapNotNull { action ->
            try {
                // record is a kotlinx.serialization.json.JsonObject
                kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromJsonElement(MorningBriefingEvent.serializer(), action.record)
            } catch (e: Exception) {
                Log.e("SupabaseRealtime", "Failed to decode realtime payload: ${e.message}")
                null
            }
        }
    }

    suspend fun disconnectChannel(patientId: String) {
        try {
            val channel = supabase.channel("public:daily_morning_briefings:patient_id=eq.$patientId")
            channel.unsubscribe()
        } catch (e: Exception) {
            Log.e("SupabaseRealtime", "Failed to disconnect realtime channel: ${e.message}")
        }
    }
}
