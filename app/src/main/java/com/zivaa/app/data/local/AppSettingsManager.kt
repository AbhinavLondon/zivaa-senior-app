package com.zivaa.app.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _darkThemeFlow = MutableStateFlow(prefs.getBoolean("dark_theme", false))
    val darkThemeFlow: StateFlow<Boolean> = _darkThemeFlow.asStateFlow()
    
    private val _showLongevityPlanFlow = MutableStateFlow(prefs.getBoolean("show_longevity_plan", true))
    val showLongevityPlanFlow: StateFlow<Boolean> = _showLongevityPlanFlow.asStateFlow()

    private val _preferredLanguageFlow = MutableStateFlow(prefs.getString("preferred_language", "English") ?: "English")
    val preferredLanguageFlow: StateFlow<String> = _preferredLanguageFlow.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean("dark_theme", enabled).apply()
        _darkThemeFlow.value = enabled
    }
    
    fun setShowLongevityPlan(enabled: Boolean) {
        prefs.edit().putBoolean("show_longevity_plan", enabled).apply()
        _showLongevityPlanFlow.value = enabled
    }

    fun setPreferredLanguage(language: String) {
        prefs.edit().putString("preferred_language", language).apply()
        _preferredLanguageFlow.value = language
    }
}
