package com.zivaa.app.data.remote

import com.zivaa.app.data.model.HealthPayload
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import com.google.gson.annotations.SerializedName

interface ZivaaApiService {
    @POST("api/v1/health/ingest")
    suspend fun uploadHealthData(
        @Body payload: HealthPayload
    ): Response<SyncResponse>

    @Multipart
    @POST("api/v1/fhir/Bundle/upload")
    suspend fun uploadFhirBundle(
        @Query("patient_id") patient_id: String,
        @Query("effective_date") effective_date: String?,
        @Part file: MultipartBody.Part
    ): Response<UploadFhirResponse>

    @POST("api/v1/health/daily-plan")
    suspend fun getDailyPlan(
        @Body payload: DailyPlanPayload
    ): Response<NestedDailyPlanResponse>

    @POST("api/v1/health/trigger-tripwire")
    suspend fun triggerTripwire(
        @Body payload: TriggerTripwirePayload
    ): Response<TripwireResponse>

    @POST("api/v1/health/sync-complete")
    suspend fun syncComplete(
        @Body payload: SyncCompletePayload
    ): Response<TripwireResponse>

    @POST("api/v1/health/daily-plan/update-task")
    suspend fun updateTaskStatus(
        @Body payload: UpdateTaskPayload
    ): Response<TripwireResponse>

    @retrofit2.http.GET("api/v1/health/lab-category-summary/{report_id}/{category}")
    suspend fun getCategorySummary(
        @retrofit2.http.Path("report_id") reportId: String,
        @retrofit2.http.Path("category") category: String
    ): Response<CategorySummaryResponse>

    @POST("api/register-device")
    suspend fun registerDevice(
        @Body request: DeviceRegistrationRequest
    ): Response<TripwireResponse>

    @POST("api/v1/coach/chat")
    suspend fun sendCoachMessage(
        @Body request: CoachChatRequest
    ): Response<CoachChatResponse>

    @retrofit2.http.Streaming
    @POST("api/v1/coach/chat/stream")
    suspend fun streamCoachMessage(
        @Body request: CoachChatRequest
    ): Response<okhttp3.ResponseBody>
    
    @POST("api/v1/coach/chat/symptom_reply")
    suspend fun sendSymptomReply(
        @Body request: SymptomReplyRequest
    ): Response<CoachChatResponse>

    @Multipart
    @POST("api/v1/nutrition/analyze-meal-photo")
    suspend fun analyzeMealPhoto(
        @Part file: MultipartBody.Part
    ): Response<NutritionAnalysisResponse>

    @POST("api/v1/nutrition/analyze-meal-text")
    suspend fun analyzeMealText(
        @Body request: AnalyzeMealTextRequest
    ): Response<NutritionAnalysisResponse>

    @POST("api/v1/nutrition/log-meal")
    suspend fun logMeal(
        @Body request: LogMealRequest
    ): Response<TripwireResponse> // Reusing generic response

    @GET("api/v1/nutrition/meals")
    suspend fun getMeals(
        @Query("patient_id") patientId: String,
        @Query("date") date: String
    ): Response<List<FoodItemDto>>

    @retrofit2.http.DELETE("api/v1/nutrition/meals/{meal_id}")
    suspend fun deleteMeal(
        @retrofit2.http.Path("meal_id") mealId: String
    ): Response<TripwireResponse>

    @POST("api/v1/nutrition/analyze-barcode")
    suspend fun analyzeBarcode(
        @Body request: AnalyzeBarcodeRequest
    ): Response<NutritionAnalysisResponse>

    @GET("api/v1/health/memory/longevity-plan/{patient_id}")
    suspend fun getLongevityPlan(
        @retrofit2.http.Path("patient_id") patientId: String
    ): Response<com.zivaa.app.data.model.LongevityPlanResponse>

    @retrofit2.http.PUT("api/v1/health/memory/symptoms/{symptom_id}")
    suspend fun updateSymptom(
        @retrofit2.http.Path("symptom_id") symptomId: String,
        @Body payload: com.zivaa.app.data.model.UpdateSymptomPayload
    ): Response<TripwireResponse>

    @retrofit2.http.PUT("api/v1/health/memory/actions/{action_id}")
    suspend fun updateAction(
        @retrofit2.http.Path("action_id") actionId: String,
        @Body payload: com.zivaa.app.data.model.UpdateActionPayload
    ): Response<TripwireResponse>

    @GET("api/v1/longevity/protocols/{patient_id}")
    suspend fun getLongevityProtocols(
        @retrofit2.http.Path("patient_id") patientId: String,
        @Query("date") date: String
    ): Response<com.zivaa.app.data.model.LongevityProtocolListResponse>

    @POST("api/v1/longevity/protocols/{patient_id}/complete/{protocol_id}")
    suspend fun completeLongevityProtocol(
        @retrofit2.http.Path("patient_id") patientId: String,
        @retrofit2.http.Path("protocol_id") protocolId: String
    ): Response<TripwireResponse>

    @GET("api/v1/coach/chat/history/{patient_id}")
    suspend fun getCoachChatLogs(
        @Path("patient_id") patientId: String
    ): Response<List<CoachChatLog>>

    @GET("api/v1/coach/chat/sessions/{patient_id}")
    suspend fun getCoachSessions(
        @Path("patient_id") patientId: String
    ): Response<List<CoachSessionSummary>>

    @GET("api/v1/coach/chat/session/{session_id}")
    suspend fun getCoachSessionMessages(
        @Path("session_id") sessionId: String
    ): Response<List<CoachChatLog>>
}

data class DailyPlanPayload(
    val vitals: Map<String, Double>? = null,
    val conditions: List<String> = emptyList(),
    val patient_id: String? = null,
    val location: String? = null
)

data class DailyPlanTask(
    val task: String,
    val time: String? = null,
    val category: String? = null,
    val details: String? = null,
    val completed: Boolean = false
)

data class DailyPlanSchedule(
    val morning: List<DailyPlanTask>? = null,
    val afternoon: List<DailyPlanTask>? = null,
    val evening: List<DailyPlanTask>? = null,
    val night: List<DailyPlanTask>? = null
)

data class DailyPlanResponse(
    val summary: String,
    val schedule: DailyPlanSchedule,
    val health_context: Map<String, Any>? = null
)

data class NestedDailyPlanResponse(
    val summary: String,
    val schedule: DailyPlanSchedule,
    val health_context: Map<String, Any>? = null
)

data class FoodItemResult(
    val name: String,
    val calories: Double = 0.0,
    @SerializedName("carbs_g", alternate = ["carbs"])
    val carbs: Double = 0.0,
    @SerializedName("protein_g", alternate = ["protein"])
    val protein: Double = 0.0,
    @SerializedName("fat_g", alternate = ["fat"])
    val fat: Double = 0.0,
    val confidence: Double? = null,
    val serving_size: String? = null
)

data class AnalyzeMealTextRequest(
    val text: String,
    val location: String? = null
)

data class LogMealRequest(
    val patient_id: String? = null, 
    val meal_type: String? = null, 
    val meal_name: String? = null, 
    val meal_quantity: Double? = null, 
    val logging_method: String? = null, 
    val ai_analysis: String? = null, 
    val foods: List<FoodItemResult>? = null, 
    val food_items: List<FoodItemResult>? = null, 
    val date: String? = null
)

data class AnalyzeBarcodeRequest(
    val barcode: String
)

data class TripwireResponse(val status: String? = null, val message: String? = null)
data class SyncCompletePayload(val patient_id: String? = null, val timezone: String? = null, val sync_type: String? = null, val records_synced: Int? = null, val metric_types: List<String>? = null)
data class UpdateTaskPayload(val patient_id: String? = null, val period: String? = null, val task_index: Int? = null, val completed: Boolean? = null)
data class CategorySummaryResponse(val summary: String? = null)
data class DeviceRegistrationRequest(val device_id: String? = null, val token: String? = null, val fcm_token: String? = null, val patient_id: String? = null, val timezone: String? = null)
data class CoachChatRequest(
    val patient_id: String? = null,
    val message: String? = null,
    val timezone: String? = null,
    val session_id: String? = null,
    @SerializedName("preferred_language") val preferred_language: String? = null
)
data class CoachChatResponse(val patient_id: String? = null, val reply: String = "", val suggested_actions: List<String>? = null, val acuity_level: String? = null, val timestamp: String? = null, val session_id: String? = null)
data class CoachSessionSummary(val session_id: String = "", val title: String = "", val preview: String? = null, val message_count: Int = 0, val created_at: String = "", val updated_at: String = "")
data class SymptomReplyRequest(val patient_id: String? = null, val symptom_id: String? = null, val reply_status: String = "")
data class NutritionAnalysisResponse(val meal_name: String? = null, val foods: List<FoodItemResult> = emptyList(), val analysis: String? = null, val error: String? = null)
data class NudgeAlert(val time: String? = null, val title: String? = null, val message: String? = null, val type: String? = null)
data class CoachChatLog(val id: String = "", val patient_id: String = "", val role: String = "", val message: String = "", val created_at: String = "")

data class SyncResponse(val status: String? = null, val processed_count: Int? = null, val nudge_alert: NudgeAlert? = null)
data class UploadFhirResponse(val status: String? = null, val message: String? = null)
data class TriggerTripwirePayload(val patient_id: String? = null)
data class FoodItemDto(val id: String = "", val meal_type: String = "", val food_name: String = "", val name: String = "", val calories: Int = 0, val protein_g: Int = 0, val carbs_g: Int = 0, val fat_g: Int = 0, val protein: Int = 0, val carbs: Int = 0, val fat: Int = 0)
