package com.zivaa.app.presentation.wellness.chooseareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.SupabaseApiService
import com.zivaa.app.data.remote.SupabaseExerciseRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExerciseListViewModel(
    private val apiService: SupabaseApiService,
    private val bodyPart: String
) : ViewModel() {

    private val _exercises = MutableStateFlow<List<SupabaseExerciseRecord>>(emptyList())
    val exercises: StateFlow<List<SupabaseExerciseRecord>> = _exercises.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _availableTypes = MutableStateFlow<List<String>>(emptyList())
    val availableTypes: StateFlow<List<String>> = _availableTypes.asStateFlow()
    
    private val _availableDifficulties = MutableStateFlow<List<String>>(emptyList())
    val availableDifficulties: StateFlow<List<String>> = _availableDifficulties.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow<String?>(null)
    val selectedDifficulty: StateFlow<String?> = _selectedDifficulty.asStateFlow()

    private var allExercises = listOf<SupabaseExerciseRecord>()

    init {
        fetchExercises()
    }

    fun fetchExercises() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Using ilike for case-insensitive matching in Supabase
                val response = apiService.getExercises(bodyPartQuery = "ilike.*${bodyPart}*")
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    allExercises = data
                    
                    _availableTypes.value = data.mapNotNull { it.type }.distinct().sorted()
                    _availableDifficulties.value = data.mapNotNull { it.difficulty }.distinct().sorted()
                    
                    applyFilters()
                } else {
                    _error.value = "Failed to load exercises: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectType(type: String?) {
        _selectedType.value = if (_selectedType.value == type) null else type
        applyFilters()
    }

    fun selectDifficulty(difficulty: String?) {
        _selectedDifficulty.value = if (_selectedDifficulty.value == difficulty) null else difficulty
        applyFilters()
    }

    private fun applyFilters() {
        val currentType = _selectedType.value
        val currentDifficulty = _selectedDifficulty.value

        var filtered = allExercises

        if (currentType != null) {
            filtered = filtered.filter { it.type == currentType }
        }
        
        if (currentDifficulty != null) {
            filtered = filtered.filter { it.difficulty == currentDifficulty }
        }
        
        _exercises.value = filtered
    }
}

class ExerciseListViewModelFactory(
    private val apiService: SupabaseApiService,
    private val bodyPart: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseListViewModel(apiService, bodyPart) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
