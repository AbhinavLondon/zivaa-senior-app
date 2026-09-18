package com.zivaa.app.presentation.movement

import androidx.health.connect.client.records.StepsRecord
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.roundToInt

enum class CadenceTier(
    val title: String,
    val description: String,
    val icon: String
) {
    BRISK_CONFIDENT("Brisk & Confident", "Strong balance & aerobic vitality", "⚡"),
    STEADY_STABLE("Steady & Stable", "Consistent community walking pace", "✓"),
    GENTLE_MOBILITY("Gentle Mobility", "Relaxed pacing for joint flexibility", "🌱"),
    RESTING("Resting", "Minimal active movement", "—")
}

enum class MobilityTier(
    val title: String,
    val description: String,
    val colorHex: Long
) {
    OPTIMAL("Optimal Mobility", "Peak functional vitality & longevity", 0xFF658A73),
    STEADY("Steady & Active", "Strong daily community baseline", 0xFF4D725C),
    BUILDING("Building Rhythm", "Healthy base, room to increase pace or breaks", 0xFFD89B48),
    GENTLE("Gentle Mobility", "Low movement volume or prolonged sitting", 0xFF8A9A86)
}

data class MobilityScore(
    val overallScore: Int,                 // 0..100
    val tier: MobilityTier,
    val volumeScore: Int,                  // 0..40
    val paceScore: Int,                    // 0..20
    val activeTimeScore: Int,              // 0..20
    val regularityScore: Int,              // 0..20
    val coachingTakeaway: String
)

data class MobilitySummary(
    // KPI 1: Movement Volume & Ground Covered
    val totalSteps: Int,
    val formattedSteps: String,
    val goalSteps: Int?,
    val goalProgress: Float?,
    val distanceKm: Float,
    val activeCaloriesKcal: Int,

    // KPI 2: Walking Cadence & Pace Quality
    val cadenceSpm: Int,
    val cadenceTier: CadenceTier,
    val cadenceTitle: String,

    // KPI 3: Active Moving Time
    val activeMinutes: Int,
    val formattedActiveTime: String,

    // KPI 4: Movement Regularity (Active Daytime Hours)
    val activeHoursCount: Int,
    val elapsedDaytimeHours: Int,
    val targetActiveHours: Int = 8,
    val stepThresholdPerHour: Int = 150,
    val regularityRatioText: String,
    val regularityStatus: String,

    // Composite Mobility Score (40% Steps, 20% Cadence, 20% Active Time, 20% Regularity)
    val mobilityScore: MobilityScore,

    // Presentation & Context
    val primarySourceLabel: String,
    val clinicalNarrative: String
)

object MobilityCalculator {

    fun calculateMobilitySummary(
        todayStepsRecords: List<StepsRecord>,
        hourlySteps: List<Pair<String, Int>>,
        resolvedTotalSteps: Int? = null,
        measuredDistanceMeters: Double? = null,
        measuredActiveCalories: Double? = null,
        goalSteps: Int? = null,
        winningSourcePackage: String? = null,
        zone: ZoneId = ZoneId.systemDefault(),
        targetActiveHours: Int = 8
    ): MobilitySummary {
        // 1. Filter granular walking bouts (exclude 24h / multi-hour summaries >600s or <=0s)
        val granularRecords = todayStepsRecords.filter { record ->
            val durationSec = Duration.between(record.startTime, record.endTime).seconds
            durationSec in 5..600 && record.count > 0
        }

        // 2. Identify the primary source:
        // Prefer the source that has granular walking bouts. If winningSourcePackage has granular records, use it;
        // otherwise pick the source that contributed the highest steps among granular records.
        val primarySource = if (winningSourcePackage != null && granularRecords.any { it.metadata.dataOrigin.packageName == winningSourcePackage }) {
            winningSourcePackage
        } else {
            granularRecords.groupBy { it.metadata.dataOrigin.packageName }
                .maxByOrNull { entry -> entry.value.sumOf { it.count } }
                ?.key ?: winningSourcePackage
        }

        // Filter bouts belonging to the selected source (or all granular if none matched)
        val validBouts = if (primarySource != null) {
            val boutsForSource = granularRecords.filter { it.metadata.dataOrigin.packageName == primarySource }
            if (boutsForSource.isNotEmpty()) boutsForSource else granularRecords
        } else {
            granularRecords
        }

        // 3. KPI 1: Volume & Ground Covered
        val totalSteps = resolvedTotalSteps ?: if (validBouts.isNotEmpty()) {
            validBouts.sumOf { it.count.toInt() }
        } else {
            todayStepsRecords.filter { Duration.between(it.startTime, it.endTime).seconds <= 600 }
                .sumOf { it.count.toInt() }
        }

        val formattedSteps = java.text.NumberFormat.getNumberInstance().format(totalSteps)

        val goalProgress = if (goalSteps != null && goalSteps > 0) {
            (totalSteps.toFloat() / goalSteps.toFloat()).coerceIn(0f, 1f)
        } else null

        val distanceKm = if (measuredDistanceMeters != null && measuredDistanceMeters > 0) {
            (measuredDistanceMeters / 1000.0).toFloat()
        } else {
            (totalSteps * 0.72f) / 1000.0f
        }

        val activeCaloriesKcal = if (measuredActiveCalories != null && measuredActiveCalories > 0) {
            measuredActiveCalories.roundToInt()
        } else {
            (totalSteps * 0.04f).roundToInt()
        }

        // 4 & 5. KPI 2 & 3: Walking Cadence and Active Moving Time
        // Compute individual pacing for each bout
        data class BoutPacing(val bout: StepsRecord, val durationSec: Long, val spm: Double)

        val boutPacings = validBouts.map { bout ->
            val durationSec = Duration.between(bout.startTime, bout.endTime).seconds.coerceAtLeast(1)
            val spm = (bout.count.toDouble() / durationSec.toDouble()) * 60.0
            BoutPacing(bout, durationSec, spm)
        }

        // Active walking bouts: intervals where pacing is at walking pace (>= 40 spm) and duration >= 10s
        val activeWalkingBouts = boutPacings.filter {
            it.spm in 40.0..160.0 && it.durationSec >= 10 && it.bout.count >= 8
        }

        val walkingBoutSteps = activeWalkingBouts.sumOf { it.bout.count }
        val walkingBoutMinutes = activeWalkingBouts.sumOf { it.durationSec } / 60.0

        val cadenceSpm = if (walkingBoutMinutes >= 0.25 && walkingBoutSteps > 0) {
            (walkingBoutSteps / walkingBoutMinutes).roundToInt().coerceIn(0, 160)
        } else if (boutPacings.isNotEmpty()) {
            // Fallback: top 50% paced bouts to filter out passive standing
            val topPaced = boutPacings.sortedByDescending { it.spm }.take(maxOf(1, boutPacings.size / 2))
            val topMinutes = topPaced.sumOf { it.durationSec } / 60.0
            if (topMinutes > 0) (topPaced.sumOf { it.bout.count } / topMinutes).roundToInt().coerceIn(0, 160) else 0
        } else 0

        val cadenceTier = when {
            cadenceSpm >= 90 -> CadenceTier.BRISK_CONFIDENT
            cadenceSpm >= 70 -> CadenceTier.STEADY_STABLE
            cadenceSpm >= 45 -> CadenceTier.GENTLE_MOBILITY
            else -> CadenceTier.RESTING
        }

        // Active Moving Time:
        // Sum continuous walking minutes + estimated active stepping time for intermittent bouts
        val intermittentBouts = boutPacings.filter { it !in activeWalkingBouts }
        val intermittentMinutes = intermittentBouts.sumOf { it.bout.count } / 75.0 // ~75 spm typical stepping rate
        val activeMinutes = (walkingBoutMinutes + intermittentMinutes).roundToInt().coerceAtLeast(
            if (totalSteps > 100) (totalSteps / 80.0).roundToInt() else 0
        )
        val formattedActiveTime = when {
            activeMinutes < 60 -> "$activeMinutes mins on feet"
            else -> "${activeMinutes / 60} hr ${activeMinutes % 60} mins on feet"
        }

        // 6. KPI 4: Movement Regularity (Active Daytime Hours)
        val now = ZonedDateTime.now(zone)
        val currentHour = now.hour
        val elapsedDaytimeHours = when {
            currentHour < 8 -> 1
            currentHour > 20 -> 12
            else -> currentHour - 8 + 1
        }

        val isSmartwatch = isWatchSource(primarySource)
        val stepThresholdPerHour = if (isSmartwatch) 150 else 100

        val activeHoursCount = hourlySteps.filterIndexed { hour, pair ->
            hour in 8..minOf(20, currentHour) && pair.second >= stepThresholdPerHour
        }.size

        val regularityRatio = activeHoursCount.toFloat() / elapsedDaytimeHours.toFloat()
        val regularityStatus = when {
            activeHoursCount >= targetActiveHours || regularityRatio >= 0.60f -> "Well Distributed"
            activeHoursCount >= maxOf(2, (targetActiveHours * 0.5f).roundToInt()) || regularityRatio >= 0.35f -> "Moderate Breaks"
            else -> "Prolonged Sitting"
        }
        val regularityRatioText = "$activeHoursCount of $targetActiveHours target hrs"

        // 7. Source Label
        val primarySourceLabel = formatSourceLabel(primarySource)

        // 8. Composite Mobility Score (40% Steps, 20% Cadence, 20% Active Time, 20% Regularity)
        val mobilityScore = calculateMobilityScore(
            totalSteps = totalSteps,
            goalSteps = goalSteps,
            cadenceSpm = cadenceSpm,
            activeMinutes = activeMinutes,
            activeHoursCount = activeHoursCount,
            elapsedDaytimeHours = elapsedDaytimeHours,
            isCompletedDay = false,
            targetActiveHours = targetActiveHours
        )

        // 9. Clinical Narrative
        val clinicalNarrative = generateClinicalNarrative(
            cadenceTier = cadenceTier,
            cadenceSpm = cadenceSpm,
            activeMinutes = activeMinutes,
            regularityStatus = regularityStatus,
            activeHoursCount = activeHoursCount,
            elapsedDaytimeHours = elapsedDaytimeHours,
            totalSteps = totalSteps
        )

        return MobilitySummary(
            totalSteps = totalSteps,
            formattedSteps = formattedSteps,
            goalSteps = goalSteps,
            goalProgress = goalProgress,
            distanceKm = distanceKm,
            activeCaloriesKcal = activeCaloriesKcal,
            cadenceSpm = cadenceSpm,
            cadenceTier = cadenceTier,
            cadenceTitle = cadenceTier.title,
            activeMinutes = activeMinutes,
            formattedActiveTime = formattedActiveTime,
            activeHoursCount = activeHoursCount,
            elapsedDaytimeHours = elapsedDaytimeHours,
            targetActiveHours = targetActiveHours,
            stepThresholdPerHour = stepThresholdPerHour,
            regularityRatioText = regularityRatioText,
            regularityStatus = regularityStatus,
            mobilityScore = mobilityScore,
            primarySourceLabel = primarySourceLabel,
            clinicalNarrative = clinicalNarrative
        )
    }

    fun isWatchSource(rawSource: String?): Boolean {
        if (rawSource.isNullOrBlank()) return false
        val s = rawSource.lowercase()
        return s.contains("watch") || s.contains("fitbit") || s.contains("garmin") || s.contains("shealth") || s.contains("withings")
    }

    private fun formatSourceLabel(rawSource: String?): String {
        if (rawSource.isNullOrBlank()) return "Phone Pedometer"
        return when {
            rawSource.contains("shealth", ignoreCase = true) -> "Samsung Health"
            rawSource.contains("fitbit", ignoreCase = true) -> "Fitbit"
            rawSource.contains("garmin", ignoreCase = true) -> "Garmin"
            rawSource.contains("withings", ignoreCase = true) -> "Withings"
            rawSource.contains("fitness", ignoreCase = true) -> "Google Fit"
            rawSource.contains("healthconnect", ignoreCase = true) -> "Phone Pedometer"
            rawSource.contains("watch", ignoreCase = true) -> "Smartwatch"
            rawSource.contains("phone", ignoreCase = true) -> "Phone Pedometer"
            else -> rawSource.substringAfterLast(".").replaceFirstChar { it.uppercase() }
        }
    }

    fun fromFallback(
        totalSteps: Int,
        formattedSteps: String,
        goalSteps: Int?,
        distanceMeters: Double?,
        activeCalories: Double?,
        avgCadenceSpm: Double?,
        activeMovementMinutes: Double?,
        activeHoursCount: Int?,
        sourceLabel: String = "Phone Pedometer",
        zone: ZoneId = ZoneId.systemDefault(),
        targetActiveHours: Int = 8
    ): MobilitySummary {
        val now = ZonedDateTime.now(zone)
        val currentHour = now.hour
        val elapsedDaytimeHours = when {
            currentHour < 8 -> 1
            currentHour > 20 -> 12
            else -> currentHour - 8 + 1
        }
        val cadenceInt = avgCadenceSpm?.roundToInt() ?: 0
        val activeMinsInt = activeMovementMinutes?.roundToInt() ?: 0
        val activeHrs = activeHoursCount ?: 0

        val cadenceTier = when {
            cadenceInt >= 90 -> CadenceTier.BRISK_CONFIDENT
            cadenceInt >= 70 -> CadenceTier.STEADY_STABLE
            cadenceInt >= 45 -> CadenceTier.GENTLE_MOBILITY
            else -> CadenceTier.RESTING
        }

        val regularityRatio = activeHrs.toFloat() / elapsedDaytimeHours.toFloat()
        val regularityStatus = when {
            activeHrs >= targetActiveHours || regularityRatio >= 0.60f -> "Well Distributed"
            activeHrs >= maxOf(2, (targetActiveHours * 0.5f).roundToInt()) || regularityRatio >= 0.35f -> "Moderate Breaks"
            else -> "Prolonged Sitting"
        }

        val clinicalNarrative = generateClinicalNarrative(
            cadenceTier = cadenceTier,
            cadenceSpm = cadenceInt,
            activeMinutes = activeMinsInt,
            regularityStatus = regularityStatus,
            activeHoursCount = activeHrs,
            elapsedDaytimeHours = elapsedDaytimeHours,
            totalSteps = totalSteps
        )

        val formattedActiveTime = when {
            activeMinsInt < 60 -> "$activeMinsInt mins on feet"
            else -> "${activeMinsInt / 60} hr ${activeMinsInt % 60} mins on feet"
        }

        val goalProgress = if (goalSteps != null && goalSteps > 0) {
            (totalSteps.toFloat() / goalSteps.toFloat()).coerceIn(0f, 1f)
        } else null

        val distanceKm = if (distanceMeters != null && distanceMeters > 0) {
            (distanceMeters / 1000.0).toFloat()
        } else {
            (totalSteps * 0.72f) / 1000.0f
        }

        val activeCal = if (activeCalories != null && activeCalories > 0) {
            activeCalories.roundToInt()
        } else {
            (totalSteps * 0.04f).roundToInt()
        }

        val mobilityScore = calculateMobilityScore(
            totalSteps = totalSteps,
            goalSteps = goalSteps,
            cadenceSpm = cadenceInt,
            activeMinutes = activeMinsInt,
            activeHoursCount = activeHrs,
            elapsedDaytimeHours = elapsedDaytimeHours,
            isCompletedDay = false,
            targetActiveHours = targetActiveHours
        )

        val isSmartwatch = isWatchSource(sourceLabel)
        val stepThresholdPerHour = if (isSmartwatch) 150 else 100

        return MobilitySummary(
            totalSteps = totalSteps,
            formattedSteps = formattedSteps,
            goalSteps = goalSteps,
            goalProgress = goalProgress,
            distanceKm = distanceKm,
            activeCaloriesKcal = activeCal,
            cadenceSpm = cadenceInt,
            cadenceTier = cadenceTier,
            cadenceTitle = cadenceTier.title,
            activeMinutes = activeMinsInt,
            formattedActiveTime = formattedActiveTime,
            activeHoursCount = activeHrs,
            elapsedDaytimeHours = elapsedDaytimeHours,
            targetActiveHours = targetActiveHours,
            stepThresholdPerHour = stepThresholdPerHour,
            regularityRatioText = "$activeHrs of $targetActiveHours target hrs",
            regularityStatus = regularityStatus,
            mobilityScore = mobilityScore,
            primarySourceLabel = sourceLabel,
            clinicalNarrative = clinicalNarrative
        )
    }

    fun calculateMobilityScore(
        totalSteps: Int,
        goalSteps: Int? = null,
        cadenceSpm: Int,
        activeMinutes: Int,
        activeHoursCount: Int,
        elapsedDaytimeHours: Int = 12,
        isCompletedDay: Boolean = false,
        targetActiveHours: Int = 8
    ): MobilityScore {
        // 1. Step Volume (40% Weight / 0 to 40 pts)
        val targetSteps = if (goalSteps != null && goalSteps > 0) goalSteps else 7000
        val volumeScore = ((totalSteps.toDouble() / targetSteps.toDouble()) * 40.0)
            .coerceIn(0.0, 40.0)
            .roundToInt()

        // 2. Walking Cadence Quality (20% Weight / 0 to 20 pts)
        val paceScore = when {
            cadenceSpm >= 85 -> 20
            cadenceSpm >= 70 -> 15
            cadenceSpm >= 50 -> 10
            cadenceSpm >= 30 -> 5
            cadenceSpm > 0 -> 2
            else -> 0
        }

        // 3. Active Moving Time (20% Weight / 0 to 20 pts)
        val activeTimeScore = when {
            activeMinutes >= 30 -> 20
            activeMinutes >= 20 -> 15
            activeMinutes >= 10 -> 10
            activeMinutes >= 1 -> 5
            else -> 0
        }

        // 4. Movement Regularity (20% Weight / 0 to 20 pts)
        val target = targetActiveHours.coerceIn(4, 12)
        val tier4 = target
        val tier3 = maxOf(2, (target * 0.75f).roundToInt())
        val tier2 = maxOf(2, (target * 0.50f).roundToInt())
        val tier1 = maxOf(1, (target * 0.25f).roundToInt())

        val bankedPoints = when {
            activeHoursCount >= tier4 -> 20
            activeHoursCount >= tier3 -> 15
            activeHoursCount >= tier2 -> 10
            activeHoursCount >= tier1 -> 5
            activeHoursCount > 0 -> 2
            else -> 0
        }

        val regularityScore = if (isCompletedDay || elapsedDaytimeHours >= 12) {
            bankedPoints
        } else {
            // For in-progress day, scale against elapsed daytime hours, but protect banked hours
            val ratio = activeHoursCount.toFloat() / elapsedDaytimeHours.coerceAtLeast(1).toFloat()
            val ratioPoints = when {
                ratio >= 0.65f -> 20
                ratio >= 0.50f -> 15
                ratio >= 0.33f -> 10
                ratio >= 0.15f -> 5
                ratio > 0f -> 2
                else -> 0
            }
            maxOf(bankedPoints, ratioPoints)
        }

        val overallScore = (volumeScore + paceScore + activeTimeScore + regularityScore).coerceIn(0, 100)

        val tier = when {
            overallScore >= 85 -> MobilityTier.OPTIMAL
            overallScore >= 70 -> MobilityTier.STEADY
            overallScore >= 50 -> MobilityTier.BUILDING
            else -> MobilityTier.GENTLE
        }

        val coachingTakeaway = when {
            overallScore >= 85 ->
                "Outstanding functional balance today! Keep up this wonderful daily rhythm."
            volumeScore < 20 ->
                "A gentle 10-minute walk will add ~800 steps and lift your volume score."
            regularityScore < 15 ->
                "Moving for a few minutes each hour this afternoon will raise your regularity score."
            paceScore < 15 ->
                "A slightly brisker walking pace during your next walk will lift your pace quality."
            activeTimeScore < 15 ->
                "Spending another 10 minutes upright on your feet will reach your active time goal."
            else ->
                "Consistent daily movement! Taking one more light stroll will push you to Optimal."
        }

        return MobilityScore(
            overallScore = overallScore,
            tier = tier,
            volumeScore = volumeScore,
            paceScore = paceScore,
            activeTimeScore = activeTimeScore,
            regularityScore = regularityScore,
            coachingTakeaway = coachingTakeaway
        )
    }

    private fun generateClinicalNarrative(
        cadenceTier: CadenceTier,
        cadenceSpm: Int,
        activeMinutes: Int,
        regularityStatus: String,
        activeHoursCount: Int,
        elapsedDaytimeHours: Int,
        totalSteps: Int
    ): String {
        return when {
            totalSteps < 100 -> "Resting or light morning movement. Taking small strolls throughout the day keeps joints flexible."
            cadenceTier == CadenceTier.BRISK_CONFIDENT && regularityStatus == "Well Distributed" ->
                "Excellent functional vitality today. Your brisk pace ($cadenceSpm spm) and regular movement breaks support cardiovascular strength and joint mobility."
            cadenceTier == CadenceTier.BRISK_CONFIDENT ->
                "Great brisk walking pace ($cadenceSpm spm). Spreading more short walks across the remaining hours will help prevent sedentary stiffness."
            cadenceTier == CadenceTier.STEADY_STABLE && regularityStatus == "Well Distributed" ->
                "Consistent, stable movement spread nicely across $activeHoursCount of $elapsedDaytimeHours daytime hours. Ideal for balance and circulation."
            cadenceTier == CadenceTier.STEADY_STABLE ->
                "Solid, steady walking pace ($cadenceSpm spm) with $activeMinutes minutes on your feet. Keep up regular movement breaks."
            regularityStatus == "Prolonged Sitting" ->
                "You have been sitting for extended periods today. A gentle 5-minute stroll will boost circulation and loosen muscles."
            else ->
                "Gentle, relaxed mobility today ($activeMinutes mins active). Every step helps maintain joint flexibility and daily independence."
        }
    }
}
