package com.zivaa.app.presentation.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zivaa.app.data.remote.AuthManager

class MoodViewModelFactory(
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoodViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
