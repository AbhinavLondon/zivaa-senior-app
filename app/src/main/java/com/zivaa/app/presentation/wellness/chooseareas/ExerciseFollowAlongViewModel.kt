package com.zivaa.app.presentation.wellness.chooseareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zivaa.app.data.remote.SupabaseApiService
import com.zivaa.app.data.remote.SupabaseExerciseRecord
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class FollowAlongMode {
    OVERVIEW,       // Showing list of relevant exercises for this routine
    ACTIVE_PLAYER,  // Following along with the timer and instructions
    COMPLETED       // Routine finished celebratory state
}

class ExerciseFollowAlongViewModel(
    private val apiService: SupabaseApiService,
    val routineTitle: String,
    private val exerciseIds: List<String>,
    private val targetBodyPart: String? = null
) : ViewModel() {

    private val _mode = MutableStateFlow(FollowAlongMode.OVERVIEW)
    val mode: StateFlow<FollowAlongMode> = _mode.asStateFlow()

    private val _exercises = MutableStateFlow<List<SupabaseExerciseRecord>>(emptyList())
    val exercises: StateFlow<List<SupabaseExerciseRecord>> = _exercises.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _secondsRemaining = MutableStateFlow(45)
    val secondsRemaining: StateFlow<Int> = _secondsRemaining.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var timerJob: Job? = null

    init {
        fetchRelevantExercises()
    }

    private fun fetchRelevantExercises() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch exercise repository from Supabase (defaults to no body_part filter, returning repository)
                val response = apiService.getExercises(limit = 200)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val all = response.body()!!
                    android.util.Log.d("FollowAlongVM", "Loaded ${all.size} exercises from repository. Target IDs: $exerciseIds, Target Body Part: $targetBodyPart")

                    // 1. First priority: match against specific exercise IDs from table
                    val matchedByIds = if (exerciseIds.isNotEmpty()) {
                        val idMap = all.associateBy { it.id.trim() }
                        exerciseIds.mapNotNull { idMap[it.trim()] }
                    } else {
                        emptyList()
                    }

                    // 2. Second priority: match against target body part if IDs didn't match
                    val matchedByBodyPart = if (matchedByIds.isEmpty() && !targetBodyPart.isNullOrBlank()) {
                        val bpClean = targetBodyPart.lowercase().trim()
                        all.filter { ex ->
                            val bp = (ex.body_part ?: "").lowercase()
                            val name = ex.exercise_name.lowercase()
                            bp.contains(bpClean) || name.contains(bpClean)
                        }
                    } else {
                        emptyList()
                    }

                    // 3. Fallback: gentle beginner mobility / stretch exercises
                    val generalMobility = all.filter { ex ->
                        val diff = (ex.difficulty ?: "").lowercase()
                        val type = (ex.type ?: "").lowercase()
                        (diff == "beginner" || diff.isBlank()) && (type.contains("mobility") || type.contains("stretch") || type.contains("balance"))
                    }.take(3)

                    _exercises.value = when {
                        matchedByIds.isNotEmpty() -> matchedByIds
                        matchedByBodyPart.isNotEmpty() -> matchedByBodyPart.take(3)
                        generalMobility.isNotEmpty() -> generalMobility
                        else -> all.take(3)
                    }
                } else {
                    android.util.Log.w("FollowAlongVM", "Failed to load exercises: ${response.code()} ${response.message()}")
                    _exercises.value = getFallbackSeniorExercises()
                }
            } catch (e: Exception) {
                android.util.Log.e("FollowAlongVM", "Error fetching exercises", e)
                _exercises.value = getFallbackSeniorExercises()
            } finally {
                if (_exercises.value.isEmpty()) {
                    _exercises.value = getFallbackSeniorExercises()
                }
                _isLoading.value = false
            }
        }
    }

    fun startFollowAlong() {
        if (_exercises.value.isEmpty()) {
            _exercises.value = getFallbackSeniorExercises()
        }
        _currentIndex.value = 0
        _mode.value = FollowAlongMode.ACTIVE_PLAYER
        loadExerciseTimer(0)
        startTimer()
    }

    private fun getFallbackSeniorExercises(): List<SupabaseExerciseRecord> {
        return listOf(
            SupabaseExerciseRecord(
                id = "53",
                exercise_name = "Standing Hip Flexor Stretch",
                body_part = "Hip",
                type = "Stretch",
                difficulty = "Beginner",
                starting_position = "Standing",
                equipment_needed = "Sturdy chair for support",
                duration_seconds = 60,
                step_by_step_instructions = "1. Stand tall holding the back of a chair with one hand.\n2. Step your right foot back about 2 feet, toes forward.\n3. Gently tuck your tailbone under and press your right hip forward.\n4. Feel a gentle stretch across the front of the right hip and thigh.\n5. Hold, breathe slowly, then switch sides.",
                tips = "Keep your torso upright — don't lean forward. Deepen the stretch on each exhale.",
                benefits = "Improves hip mobility, counteracts sitting tightness, supports posture and walking stride."
            ),
            SupabaseExerciseRecord(
                id = "91",
                exercise_name = "Seated Knee Extension",
                body_part = "Knee",
                type = "Mobility",
                difficulty = "Beginner",
                starting_position = "Seated",
                equipment_needed = "Sturdy chair",
                duration_seconds = 45,
                step_by_step_instructions = "1. Sit tall in a firm chair with feet flat on the floor.\n2. Slowly straighten one knee until your leg is nearly straight out in front.\n3. Hold for 3 to 5 seconds while flexing your thigh muscle.\n4. Slowly lower your foot back down.\n5. Repeat 8-10 times, then switch legs.",
                tips = "Keep your back straight against the chair back. Don't lock the knee joint harshly.",
                benefits = "Strengthens the quadriceps, stabilizes the knee joint, and eases stiffness."
            ),
            SupabaseExerciseRecord(
                id = "83",
                exercise_name = "Gentle Ankle Circles & Pumps",
                body_part = "Lower Body",
                type = "Mobility",
                difficulty = "Beginner",
                starting_position = "Seated",
                equipment_needed = "Sturdy chair",
                duration_seconds = 45,
                step_by_step_instructions = "1. While seated comfortably, lift one foot slightly off the floor.\n2. Slowly rotate your ankle in a smooth circle 5 times clockwise, then 5 times counter-clockwise.\n3. Point and flex your toes up and down.\n4. Repeat with the other foot.",
                tips = "Perform smooth, pain-free circles. Great for improving circulation.",
                benefits = "Boosts circulation, reduces ankle swelling, and maintains joint flexibility."
            )
        )
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _isPlaying.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_secondsRemaining.value > 0 && _isPlaying.value) {
                delay(1000L)
                _secondsRemaining.value -= 1
            }
            if (_secondsRemaining.value <= 0 && _isPlaying.value) {
                onCurrentExerciseFinished()
            }
        }
    }

    private fun pauseTimer() {
        _isPlaying.value = false
        timerJob?.cancel()
    }

    private fun onCurrentExerciseFinished() {
        val nextIdx = _currentIndex.value + 1
        if (nextIdx < _exercises.value.size) {
            _currentIndex.value = nextIdx
            loadExerciseTimer(nextIdx)
            startTimer()
        } else {
            finishRoutine()
        }
    }

    fun skipToNext() {
        pauseTimer()
        onCurrentExerciseFinished()
    }

    fun skipToPrevious() {
        pauseTimer()
        val prevIdx = (_currentIndex.value - 1).coerceAtLeast(0)
        _currentIndex.value = prevIdx
        loadExerciseTimer(prevIdx)
        startTimer()
    }

    private fun loadExerciseTimer(index: Int) {
        val currentEx = _exercises.value.getOrNull(index)
        _secondsRemaining.value = currentEx?.duration_seconds ?: 45
    }

    fun finishRoutine() {
        pauseTimer()
        _mode.value = FollowAlongMode.COMPLETED
    }

    fun backToOverview() {
        pauseTimer()
        _mode.value = FollowAlongMode.OVERVIEW
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

class ExerciseFollowAlongViewModelFactory(
    private val apiService: SupabaseApiService,
    private val routineTitle: String,
    private val exerciseIds: List<String>,
    private val targetBodyPart: String? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseFollowAlongViewModel::class.java)) {
            return ExerciseFollowAlongViewModel(apiService, routineTitle, exerciseIds, targetBodyPart) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
