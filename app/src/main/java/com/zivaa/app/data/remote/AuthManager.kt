package com.zivaa.app.data.remote

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AuthManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "zivaa_auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // If the KeyStore is wiped but the SharedPreferences file remains (common on reinstall),
        // we must delete the corrupted file and try again.
        try {
            val prefFile = java.io.File(context.applicationInfo.dataDir + "/shared_prefs/zivaa_auth_prefs.xml")
            if (prefFile.exists()) {
                prefFile.delete()
            }
        } catch (ignored: Exception) {}
        
        EncryptedSharedPreferences.create(
            context,
            "zivaa_auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthManager? = null

        private val _authEvents = MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 2)
        val authEvents = _authEvents.asSharedFlow()

        fun getInstance(context: Context): AuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    val authEvents = Companion.authEvents

    fun saveSession(
        accessToken: String, 
        refreshToken: String, 
        userId: String,
        email: String? = null,
        fullName: String? = null
    ) {
        val editor = sharedPreferences.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putString("user_id", userId)
        if (!email.isNullOrBlank()) {
            editor.putString("user_email", email)
        }
        if (!fullName.isNullOrBlank()) {
            editor.putString("full_name", fullName)
        }
        editor.apply()
        _authEvents.tryEmit(true)
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString("user_email", null)
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    fun getUserId(): String? {
        return sharedPreferences.getString("user_id", null)
    }
    
    fun savePatientProfile(fullName: String, locationCity: String?, createdAt: String?, dob: String?, profilePicUrl: String?, preferredLanguage: String? = null) {
        val editor = sharedPreferences.edit()
            .putString("full_name", fullName)
            .putString("location_city", locationCity)
            .putString("created_at", createdAt)
            .putString("dob", dob)
            .putString("profile_pic_url", profilePicUrl)
        if (preferredLanguage != null) {
            editor.putString("preferred_language", preferredLanguage)
        }
        editor.apply()
    }
    
    fun getPatientProfile(): Map<String, String?> {
        return mapOf(
            "full_name" to sharedPreferences.getString("full_name", null),
            "location_city" to sharedPreferences.getString("location_city", null),
            "created_at" to sharedPreferences.getString("created_at", null),
            "dob" to sharedPreferences.getString("dob", null),
            "profile_pic_url" to sharedPreferences.getString("profile_pic_url", null),
            "preferred_language" to sharedPreferences.getString("preferred_language", null)
        )
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
        _authEvents.tryEmit(false)
    }

    fun hasValidSession(): Boolean {
        return getAccessToken() != null
    }
}
