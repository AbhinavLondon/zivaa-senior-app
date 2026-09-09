package com.zivaa.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SupabaseApiService {
    @Headers("Prefer: resolution=merge-duplicates")
    @POST("rest/v1/vitals_raw?on_conflict=patient_id,metric_type,recorded_at,source")
    suspend fun insertRawVitals(
        @Body payload: List<SupabaseVitalRecord>
    ): Response<Void>
    
    @Headers("Prefer: resolution=merge-duplicates")
    @POST("rest/v1/vitals_daily")
    suspend fun upsertDailyVitals(
        @Body payload: List<SupabaseDailyVitalRecord>
    ): Response<Void>

    @retrofit2.http.GET("rest/v1/vitals_daily")
    suspend fun getDailyVitals(
        @retrofit2.http.Query("patient_id") patientIdQuery: String,
        @retrofit2.http.Query("order") order: String = "date.desc",
        @retrofit2.http.Query("limit") limit: Int = 10
    ): Response<List<SupabaseDailyVitalRecord>>

    @JvmSuppressWildcards
    @retrofit2.http.GET("rest/v1/vitals_daily")
    suspend fun getDailyVitalsDynamic(
        @retrofit2.http.Query("patient_id") patientIdQuery: String,
        @retrofit2.http.Query("order") order: String = "date.desc",
        @retrofit2.http.Query("limit") limit: Int = 10
    ): Response<List<Map<String, Any>>>

    @retrofit2.http.GET("rest/v1/fhir_diagnostic_reports")
    suspend fun getDiagnosticReports(
        @retrofit2.http.Query("patient_id") patientIdQuery: String,
        @retrofit2.http.Query("select") select: String = "id,patient_id,performer,effective_datetime,resource",
        @retrofit2.http.Query("order") order: String = "created_at.desc"
    ): Response<List<SupabaseFhirDiagnosticReport>>

    @retrofit2.http.GET("rest/v1/fhir_diagnostic_reports")
    suspend fun getDiagnosticReportById(
        @retrofit2.http.Query("id") idQuery: String
    ): Response<List<SupabaseFhirDiagnosticReport>>

    @retrofit2.http.GET("rest/v1/fhir_observations")
    suspend fun getObservationsByReport(
        @retrofit2.http.Query("report_id") reportIdQuery: String
    ): Response<List<SupabaseFhirObservation>>

    @retrofit2.http.GET("rest/v1/vitals_raw")
    suspend fun getRawVitals(
        @retrofit2.http.Query("patient_id") patientIdQuery: String,
        @retrofit2.http.Query("metric_type") metricTypeQuery: String,
        @retrofit2.http.Query("order") order: String = "recorded_at.desc",
        @retrofit2.http.Query("limit") limit: Int = 1
    ): Response<List<SupabaseVitalRecord>>

    @retrofit2.http.GET("rest/v1/vitals_raw")
    suspend fun getTodayVitals(
        @retrofit2.http.Query("patient_id") patientIdQuery: String,
        @retrofit2.http.Query("metric_type") metricTypeQuery: String,
        @retrofit2.http.Query("recorded_at") recordedAtQuery: String,
        @retrofit2.http.Query("order") order: String = "recorded_at.asc",
        @retrofit2.http.Query("limit") limit: Int = 1000
    ): Response<List<SupabaseVitalRecord>>

    @retrofit2.http.GET("rest/v1/vitals_hourly")
    suspend fun getHourlyVitals(
        @retrofit2.http.Query("patient_id") patientIdQuery: String,
        @retrofit2.http.Query("order") order: String = "hour_start.desc",
        @retrofit2.http.Query("limit") limit: Int = 48
    ): Response<List<SupabaseHourlyVitalRecord>>

    @retrofit2.http.GET("rest/v1/daily_morning_briefings")
    suspend fun getMorningBriefing(
        @retrofit2.http.Query("patient_id") patientIdQuery: String,
        @retrofit2.http.Query("order") order: String = "created_at.desc",
        @retrofit2.http.Query("limit") limit: Int = 1
    ): Response<List<MorningBriefingRecord>>

    @retrofit2.http.GET("rest/v1/daily_morning_briefings")
    suspend fun getMorningBriefings(
        @retrofit2.http.Query("patient_id") patientIdQuery: String,
        @retrofit2.http.Query("select") select: String = "id,date,created_at"
    ): Response<List<MorningBriefingItem>>

    // Supabase Auth Endpoints
    @POST("rest/v1/rpc/delete_user_account")
    suspend fun deleteUserAccount(): Response<Void>

    @POST("auth/v1/otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<Void>

    @POST("auth/v1/verify")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    @JvmSuppressWildcards
    @retrofit2.http.GET("auth/v1/user")
    suspend fun getCurrentUser(): Response<Map<String, Any>>

    // Setup / Onboarding Endpoints
    @POST("rest/v1/patient_plan_setup")
    suspend fun insertPlanSetup(
        @Body payload: SupabasePatientPlanSetup
    ): Response<Void>

    @retrofit2.http.GET("rest/v1/patient_plan_setup")
    suspend fun getPlanSetup(
        @retrofit2.http.Query("patient_id") patientIdQuery: String,
        @retrofit2.http.Query("steps_goal") stepsGoalQuery: String? = null,
        @retrofit2.http.Query("order") order: String = "created_at.desc",
        @retrofit2.http.Query("limit") limit: Int = 1
    ): Response<List<SupabasePatientPlanSetup>>

    @retrofit2.http.GET("rest/v1/patients")
    suspend fun getPatient(
        @retrofit2.http.Query("id") patientIdQuery: String,
        @retrofit2.http.Query("select") select: String = "*"
    ): Response<List<PatientRecord>>

    @Headers("Prefer: return=representation,resolution=merge-duplicates")
    @POST("rest/v1/patients")
    suspend fun createPatient(@Body patient: PatientRecord): Response<List<PatientRecord>>

    @POST("rest/v1/conditions")
    suspend fun addConditions(@Body conditions: List<ConditionRecord>): Response<Void>

    @retrofit2.http.DELETE("rest/v1/conditions")
    suspend fun deleteCondition(
        @retrofit2.http.Query("id") conditionIdQuery: String,
        @retrofit2.http.Query("patient_id") patientIdQuery: String = "eq.*"
    ): Response<Void>

    @retrofit2.http.PATCH("rest/v1/nudge_alerts")
    suspend fun updateNudgeAlert(
        @retrofit2.http.Query("id") idEq: String,
        @retrofit2.http.Body request: Map<String, @JvmSuppressWildcards Any>,
        @retrofit2.http.Header("Prefer") prefer: String = "return=representation"
    ): Response<List<SupabaseNudgeAlert>>

    @POST("rest/v1/doctors")
    @Headers("Prefer: return=representation")
    suspend fun addDoctor(@Body doctor: DoctorRecord): Response<List<DoctorRecord>>

    @POST("rest/v1/caregivers")
    suspend fun addCaregivers(@Body caregivers: List<CaregiverRecord>): Response<Void>

    @retrofit2.http.GET("rest/v1/conditions")
    suspend fun getConditions(@retrofit2.http.Query("patient_id") patientIdQuery: String): Response<List<ConditionRecord>>

    @retrofit2.http.GET("rest/v1/caregivers")
    suspend fun getCaregivers(@retrofit2.http.Query("patient_id") patientIdQuery: String): Response<List<CaregiverRecord>>

    @retrofit2.http.PATCH("rest/v1/caregivers")
    suspend fun updateCaregiver(
        @retrofit2.http.Query("id") idQuery: String,
        @retrofit2.http.Query("patient_id") patientIdQuery: String = "eq.*", // safety
        @Body updates: Map<String, Any>
    ): Response<Void>
    
    @retrofit2.http.PATCH("rest/v1/patients")
    suspend fun updatePatient(
        @retrofit2.http.Query("id") idQuery: String,
        @Body updates: Map<String, String>
    ): Response<Void>
    
    @Headers("x-upsert: true")
    @POST("storage/v1/object/profile_pictures/{fileName}")
    suspend fun uploadProfilePicture(
        @retrofit2.http.Path("fileName") fileName: String,
        @Body file: okhttp3.RequestBody
    ): Response<Void>

    @POST("auth/v1/token?grant_type=refresh_token")
    fun refreshToken(@Body request: RefreshTokenRequest): retrofit2.Call<RefreshTokenResponse>

    @POST("functions/v1/generate-insight")
    suspend fun generateInsight(@Body request: InsightRequest): Response<InsightResponse>

    @POST("rest/v1/patient_checkins")
    suspend fun insertPatientCheckin(@Body checkin: SupabasePatientCheckin): Response<Void>

    @retrofit2.http.GET("rest/v1/patient_checkins")
    suspend fun getPatientCheckins(
        @retrofit2.http.Query("patient_id") patientIdQuery: String,
        @retrofit2.http.Query("order") order: String = "date.desc,created_at.desc",
        @retrofit2.http.Query("limit") limit: Int = 14
    ): Response<List<SupabasePatientCheckin>>

    @retrofit2.http.GET("rest/v1/user_insights")
    suspend fun getUserInsights(
        @retrofit2.http.Query("patient_id") patientIdQuery: String,
        @retrofit2.http.Query("insight_type") insightTypeQuery: String,
        @retrofit2.http.Query("insight_date") insightDateQuery: String,
        @retrofit2.http.Query("order") order: String = "created_at.desc",
        @retrofit2.http.Query("limit") limit: Int = 1
    ): Response<List<SupabaseUserInsight>>

    @retrofit2.http.DELETE("rest/v1/user_insights")
    suspend fun deleteUserInsights(
        @retrofit2.http.Query("patient_id") patientIdQuery: String,
        @retrofit2.http.Query("insight_type") insightTypeQuery: String? = null,
        @retrofit2.http.Query("insight_date") insightDateQuery: String? = null
    ): Response<Void>

    @retrofit2.http.GET("rest/v1/daily_plans")
    suspend fun getDailyPlans(
        @retrofit2.http.Query("patient_id") patientIdQuery: String,
        @retrofit2.http.Query("date") dateQuery: String? = null,
        @retrofit2.http.Query("order") order: String = "date.asc",
        @retrofit2.http.Query("limit") limit: Int = 7
    ): Response<List<SupabaseDailyPlanRecord>>

    @retrofit2.http.PATCH("rest/v1/daily_plans")
    suspend fun updateDailyPlan(
        @retrofit2.http.Query("id") idQuery: String,
        @Body updates: Map<String, @JvmSuppressWildcards Any>
    ): Response<Void>

    @retrofit2.http.POST("rest/v1/daily_plans")
    suspend fun insertDailyPlans(
        @Body records: List<Map<String, @JvmSuppressWildcards Any>>
    ): Response<Void>

    @Headers("Prefer: resolution=merge-duplicates")
    @retrofit2.http.POST("rest/v1/daily_plans")
    suspend fun insertDailyPlanRecords(
        @Body records: List<SupabaseDailyPlanRecord>
    ): Response<Void>

    @retrofit2.http.GET("rest/v1/nudge_alerts")
    suspend fun getNudgeAlerts(
        @retrofit2.http.Query("patient_id") patientIdQuery: String,
        @retrofit2.http.Query("created_at") createdAtQuery: String? = null,
        @retrofit2.http.Query("acknowledged") acknowledgedQuery: String? = null,
        @retrofit2.http.Query("order") order: String = "created_at.desc",
        @retrofit2.http.Query("limit") limit: Int = 10
    ): Response<List<SupabaseNudgeAlert>>

    @retrofit2.http.GET("rest/v1/coach_chat_logs")
    suspend fun getCoachChatLogs(
        @retrofit2.http.Query("patient_id") patientIdQuery: String,
        @retrofit2.http.Query("order") order: String = "created_at.asc"
    ): Response<List<SupabaseCoachChatLog>>

    @retrofit2.http.GET("rest/v1/zivaa_exercise_repository")
    suspend fun getExercises(
        @retrofit2.http.Query("body_part") bodyPartQuery: String = "eq.*",
        @retrofit2.http.Query("order") order: String = "exercise_name.asc"
    ): Response<List<SupabaseExerciseRecord>>
}

data class InsightRequest(
    val patient_id: String,
    val type: String = "daily",
    val timezone: String? = null,
    val context: String? = null
)

data class MorningBriefingRecord(
    val date: String,
    val summary: String,
    val headline: String? = null
)

data class MorningBriefingItem(
    val id: String? = null,
    val date: String? = null,
    val created_at: String? = null
)

data class InsightResponse(
    val insight: String,
    val error: String? = null
)

data class RefreshTokenRequest(
    val refresh_token: String
)

data class RefreshTokenResponse(
    val access_token: String,
    val refresh_token: String
)

data class SupabaseDailyPlanRecord(
    val id: String,
    val patient_id: String,
    val date: String?,
    val created_at: String?,
    val summary: String?,
    val schedule: DailyPlanSchedule?
)

data class SupabaseNudgeAlert(
    val id: String,
    val patient_id: String,
    val risk_level: String,
    val nudge_title: String,
    val nudge_text: String,
    val why_flagged: Any?,
    val action_steps: Any?,
    val created_at: String,
    val evidence: Map<String, @JvmSuppressWildcards Any>? = null,
    val acknowledged: Boolean? = false,
    val acknowledged_by: String? = null,
    val acknowledged_at: String? = null
)

data class SupabaseCoachChatLog(
    val id: String,
    val patient_id: String,
    val role: String,
    val message: String,
    val created_at: String,
    val metadata: Map<String, @JvmSuppressWildcards Any>? = null
)
