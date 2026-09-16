package com.zivaa.app.presentation.dashboard.tour

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class TodayTourStep(
    val stepIndex: Int,
    val title: String,
    val description: String,
    val cornerRadius: Dp,
    val padding: Dp
) {
    HERO_CARD(
        stepIndex = 1,
        title = "Daily Briefing",
        description = "A personalized morning and evening summary of your vitals and today's recommendations.",
        cornerRadius = 24.dp,
        padding = 6.dp
    ),
    GOALS_CARD(
        stepIndex = 2,
        title = "Daily Plan & Goals",
        description = "Track your movement benchmarks, nutrition targets, and health protocols. Tap any item to view or complete.",
        cornerRadius = 24.dp,
        padding = 6.dp
    ),
    STATS_STRIP(
        stepIndex = 3,
        title = "Real-Time Vitals",
        description = "Check your steps, heart rate, sleep quality, and log how you feel at any point during the day.",
        cornerRadius = 20.dp,
        padding = 6.dp
    ),
    COACH_BUTTON(
        stepIndex = 4,
        title = "AI Health Coach",
        description = "Tap 'Ask Coach' anytime for guidance on medications, meals, symptoms, or daily questions.",
        cornerRadius = 999.dp,
        padding = 6.dp
    ),
    QUICK_ACTIONS(
        stepIndex = 5,
        title = "Quick Actions (+)",
        description = "Tap '+' to quickly log meals, record your mood, or upload prescriptions and lab reports.",
        cornerRadius = 999.dp,
        padding = 6.dp
    );

    companion object {
        val TOTAL_STEPS = values().size
    }
}

@Stable
class TodayTourState(
    initialStep: TodayTourStep? = null,
    val onTourFinished: () -> Unit = {}
) {
    var currentStep by mutableStateOf<TodayTourStep?>(initialStep)
    var isTourActive by mutableStateOf(initialStep != null)
    val boundsMap = mutableStateMapOf<TodayTourStep, Rect>()

    fun updateBounds(step: TodayTourStep, bounds: Rect) {
        boundsMap[step] = bounds
    }

    fun startTour() {
        currentStep = TodayTourStep.HERO_CARD
        isTourActive = true
    }

    fun nextStep() {
        val current = currentStep ?: return
        val next = when (current) {
            TodayTourStep.HERO_CARD -> TodayTourStep.GOALS_CARD
            TodayTourStep.GOALS_CARD -> TodayTourStep.STATS_STRIP
            TodayTourStep.STATS_STRIP -> TodayTourStep.COACH_BUTTON
            TodayTourStep.COACH_BUTTON -> TodayTourStep.QUICK_ACTIONS
            TodayTourStep.QUICK_ACTIONS -> null
        }
        if (next != null) {
            currentStep = next
        } else {
            finishTour()
        }
    }

    fun previousStep() {
        val current = currentStep ?: return
        val prev = when (current) {
            TodayTourStep.HERO_CARD -> null
            TodayTourStep.GOALS_CARD -> TodayTourStep.HERO_CARD
            TodayTourStep.STATS_STRIP -> TodayTourStep.GOALS_CARD
            TodayTourStep.COACH_BUTTON -> TodayTourStep.STATS_STRIP
            TodayTourStep.QUICK_ACTIONS -> TodayTourStep.COACH_BUTTON
        }
        if (prev != null) {
            currentStep = prev
        }
    }

    fun finishTour() {
        isTourActive = false
        currentStep = null
        onTourFinished()
    }
}

@Composable
fun rememberTodayTourState(onTourFinished: () -> Unit): TodayTourState {
    return remember { TodayTourState(onTourFinished = onTourFinished) }
}
