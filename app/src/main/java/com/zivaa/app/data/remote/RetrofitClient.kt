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

    var authManager: AuthManager? = null
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
    private val tokenAuthenticator = object : okhttp3.Authenticator {
        override fun authenticate(route: okhttp3.Route?, response: okhttp3.Response): okhttp3.Request? {
            // Prevent infinite loop if the refresh token endpoint itself fails with 401
            if (response.request.url.encodedPath.contains("token")) {
                return null
            }
            
            val refreshToken = authManager?.getRefreshToken() ?: return null
            if (refreshToken.isBlank()) return null
            
            val refreshRequest = RefreshTokenRequest(refresh_token = refreshToken)
            val call = apiService.refreshToken(refreshRequest)
            val retrofitResponse = call.execute() // Synchronous execution
            
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
            
            // If we fail to refresh the token, log out the user
            authManager?.clearSession()
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
