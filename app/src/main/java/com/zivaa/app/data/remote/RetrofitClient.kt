package com.zivaa.app.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://ecwueqoxfjcepktubqrs.supabase.co/"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVjd3VlcW94ZmpjZXBrdHVicXJzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODIyMzcxOTIsImV4cCI6MjA5NzgxMzE5Mn0.2i8tPlBlNKZO1rCo2KFMXG78iZyt40EGm6_R5koDJ08"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Volatile
    var authManager: AuthManager? = null

    fun initialize(context: android.content.Context) {
        if (authManager == null) {
            authManager = AuthManager.getInstance(context)
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val token = authManager?.getAccessToken() ?: SUPABASE_ANON_KEY
        val requestBuilder = chain.request().newBuilder()
            .addHeader("apikey", SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")

        if (chain.request().header("Prefer") == null) {
            requestBuilder.addHeader("Prefer", "return=minimal")
        }

        chain.proceed(requestBuilder.build())
    }
    // Dedicated unauthenticated client for auth operations (login, refresh token)
    // NEVER attaches expired Bearer token in Authorization header
    private val authOkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val authRetrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(authOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val authApiService: SupabaseApiService = authRetrofit.create(SupabaseApiService::class.java)

    private val tokenAuthenticator = object : okhttp3.Authenticator {
        override fun authenticate(route: okhttp3.Route?, response: okhttp3.Response): okhttp3.Request? {
            // Prevent infinite loop if the refresh token endpoint itself fails with 401
            if (response.request.url.encodedPath.contains("token")) {
                return null
            }
            
            val refreshToken = authManager?.getRefreshToken() ?: return null
            if (refreshToken.isBlank()) return null
            
            val refreshRequest = RefreshTokenRequest(refresh_token = refreshToken)
            val call = authApiService.refreshToken(refreshRequest)
            val retrofitResponse = try {
                call.execute() // Synchronous execution
            } catch (e: Exception) {
                android.util.Log.e("RetrofitClient", "Exception during synchronous token refresh: ${e.message}", e)
                return null
            }
            
            if (retrofitResponse.isSuccessful) {
                val newTokens = retrofitResponse.body()
                if (newTokens != null) {
                    val userId = authManager?.getUserId() ?: ""
                    // Save new tokens
                    authManager?.saveSession(newTokens.access_token, newTokens.refresh_token, userId)
                    
                    // Retry the failed request with the new access token
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.access_token}")
                        .build()
                }
            }
            
            val errorCode = retrofitResponse.code()
            val errorBody = retrofitResponse.errorBody()?.string() ?: ""
            android.util.Log.w("RetrofitClient", "Token refresh failed with HTTP $errorCode: $errorBody")
            
            // Only clear session if server explicitly rejects refresh token as invalid or revoked.
            // Do NOT clear on 5xx server errors, rate limits, or network glitches!
            if (errorCode == 400 || errorCode == 401) {
                if (errorBody.contains("invalid_grant", ignoreCase = true) ||
                    errorBody.contains("invalid refresh token", ignoreCase = true) ||
                    errorBody.contains("refresh_token_not_found", ignoreCase = true)) {
                    android.util.Log.e("RetrofitClient", "Refresh token is permanently invalid/revoked. Clearing session.")
                    authManager?.clearSession()
                }
            }
            return null
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: SupabaseApiService = retrofit.create(SupabaseApiService::class.java)
}
