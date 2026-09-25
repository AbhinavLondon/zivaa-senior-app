package com.zivaa.app.presentation.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zivaa.app.ui.theme.ZivaaTheme
import com.zivaa.app.ui.theme.toEyebrowTitleCase
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import com.zivaa.app.data.remote.DailyPlanTask

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlanScreen(
    viewModel: com.zivaa.app.presentation.dashboard.DashboardViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToCoachChat: ((String?) -> Unit)? = null,
    onNavigateToExerciseFollowAlong: ((String, List<String>, String?, String?) -> Unit)? = null,
    onNavigateToNutrition: () -> Unit = {},
    onNavigateToHealthConnect: () -> Unit = {}
) {
    val colors = ZivaaTheme.colors
    val context = LocalContext.current
    var pendingSymptomFeedback by remember { mutableStateOf<Triple<DailyPlanTask, String, Int>?>(null) }
    
    val selectedLocalDate = remember(viewModel.selectedDate) {
        try {
            LocalDate.parse(viewModel.selectedDate)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }
    
    val dailyProgressMap = remember(viewModel.allWeeklyPlans) {
        val map = mutableMapOf<LocalDate, Pair<Int, Int>>()
        for (plan in viewModel.allWeeklyPlans) {
            try {
                val dateStr = plan.date ?: plan.created_at?.take(10) ?: continue
                val date = LocalDate.parse(dateStr)
                
                var total = 0
                var completed = 0
                val schedule = plan.schedule
                
                schedule?.morning?.let { tasks ->
                    total += tasks.size
                    completed += tasks.count { it.completed }
                }
                schedule?.afternoon?.let { tasks ->
                    total += tasks.size
                    completed += tasks.count { it.completed }
                }
                schedule?.evening?.let { tasks ->
                    total += tasks.size
                    completed += tasks.count { it.completed }
                }
                schedule?.night?.let { tasks ->
                    total += tasks.size
                    completed += tasks.count { it.completed }
                }
                
                map[date] = Pair(completed, total)
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }
        map
    }

    val allTodayTasks = viewModel.morningTasks + viewModel.afternoonTasks + viewModel.eveningTasks + viewModel.nightTasks
    val totalCount = allTodayTasks.size
    val completedCount = allTodayTasks.count { it.completed }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg),
            contentPadding = WindowInsets.systemBars.asPaddingValues()
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                PlanScreenHeader(onNavigateBack = onNavigateBack)
            }
            
            stickyHeader {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bg)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    WeeklyDaySelector(
                        selectedDate = selectedLocalDate,
                        onDateSelected = { date ->
                            viewModel.selectDate(date.toString())
                        },
                        progressMap = dailyProgressMap
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                val dateFormatted = selectedLocalDate.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault()))
                PlanScreenHero(
                    summary = viewModel.dailyPlanSummary,
                    dateString = dateFormatted,
                    completedCount = completedCount,
                    totalCount = totalCount
                )
            }

            val periods = listOf(
                "Morning" to viewModel.morningTasks,
                "Afternoon" to viewModel.afternoonTasks,
                "Evening" to viewModel.eveningTasks,
                "Night" to viewModel.nightTasks
            )

            for (period in periods) {
                val periodName = period.first
                val tasks = period.second
                
                if (tasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        PlanSectionHeader(
                            title = periodName,
                            timeRange = "", 
                            progress = "${tasks.count { it.completed }} / ${tasks.size}"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    for ((index, task) in tasks.withIndex()) {
                        item {
                            val category = task.category ?: periodName
                            val badge = task.provenance?.badge_text
                            val baseTag = category.toEyebrowTitleCase()
                            val tagText = if (!badge.isNullOrBlank()) {
                                "$badge • $baseTag"
                            } else {
                                baseTag
                            }
                            
                            val (icon, bgColor, _) = com.zivaa.app.presentation.dashboard.getGoalIconAndColors(task.task)

                            val action = task.action
                            val ctaType = action?.action_type?.uppercase()
                            val hasAction = action != null && !ctaType.isNullOrBlank() && ctaType != "CHECKBOX_ONLY"

                            val (actText, actIcon, actBg) = if (hasAction && action != null) {
                                when (ctaType) {
                                    "FOLLOW_EXERCISE" -> Triple(action.cta_label ?: "Start Routine", Icons.Default.PlayArrow, colors.sage)
                                    "LOG_VITALS" -> Triple(action.cta_label ?: "Record Vitals", Icons.Default.Favorite, Color(0xFFD9534F))
                                    "LOG_MEAL" -> Triple(action.cta_label ?: "Snap Meal", Icons.Default.Restaurant, Color(0xFFE67E22))
                                    "COACH_CHAT" -> Triple(action.cta_label ?: "Ask Zivaa", Icons.Default.ChatBubble, Color(0xFF2E7D32))
                                    "CALL_PHONE" -> Triple(action.cta_label ?: "Consult Doctor", Icons.Default.Phone, Color(0xFFC62828))
                                    else -> Triple(action.cta_label ?: "Open Action", Icons.Default.PlayArrow, colors.sage)
                                }
                            } else {
                                Triple(null, null, colors.bg)
                            }

                            val onActionClick: (() -> Unit)? = if (hasAction && action != null) {
                                {
                                    when (ctaType) {
                                        "FOLLOW_EXERCISE" -> onNavigateToExerciseFollowAlong?.invoke(
                                            action.routine_title ?: task.task,
                                            action.exercise_ids ?: emptyList(),
                                            action.target_body_part,
                                            task.id
                                        )
                                        "LOG_VITALS" -> onNavigateToHealthConnect()
                                        "LOG_MEAL" -> onNavigateToNutrition()
                                        "COACH_CHAT" -> onNavigateToCoachChat?.invoke(action.prefilled_prompt)
                                        "CALL_PHONE" -> {
                                            try {
                                                val phoneIntent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                    data = android.net.Uri.parse("tel:112")
                                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                context.startActivity(phoneIntent)
                                            } catch (e: Exception) {
                                                android.util.Log.e("PlanScreen", "Error launching phone dialer", e)
                                            }
                                        }
                                    }
                                }
                            } else null
                            
                            PlanTaskCard(
                                tag = tagText,
                                title = task.task,
                                description = task.details,
                                icon = icon,
                                iconBgColor = bgColor,
                                isCompleted = task.completed,
                                onToggleCompletion = { 
                                    if (selectedLocalDate != LocalDate.now()) return@PlanTaskCard
                                    val willBeCompleted = !task.completed
                                    val isSymptomTask = !task.symptom_id.isNullOrBlank() ||
                                        task.anchor_type?.equals("symptom", ignoreCase = true) == true ||
                                        task.provenance?.badge_text?.contains("RELIEF", ignoreCase = true) == true

                                    if (!task.id.isNullOrBlank()) {
                                        viewModel.markTaskCompletedById(task.id)
                                    } else {
                                        when (periodName.lowercase()) {
                                            "morning" -> viewModel.toggleMorningTask(index)
                                            "afternoon" -> viewModel.toggleAfternoonTask(index)
                                            "evening" -> viewModel.toggleEveningTask(index)
                                            "night" -> viewModel.toggleNightTask(index)
                                        }
                                    }

                                    if (willBeCompleted && isSymptomTask) {
                                        pendingSymptomFeedback = Triple(task, periodName.lowercase(), index)
                                    }
                                },
                                actionText = actText,
                                actionIcon = actIcon,
                                actionBgColor = actBg,
                                actionTextColor = colors.ink,
                                isEditable = (selectedLocalDate == LocalDate.now()),
                                onActionClick = onActionClick
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(130.dp))
            }
        }

        pendingSymptomFeedback?.let { (symptomTask, period, origIdx) ->
            com.zivaa.app.presentation.dashboard.components.SymptomFeedbackBottomSheet(
                task = symptomTask,
                onFeedbackSubmitted = { feedback ->
                    viewModel.submitTaskMicroFeedback(symptomTask, feedback, period, origIdx)
                    pendingSymptomFeedback = null
                },
                onDismiss = {
                    viewModel.submitTaskMicroFeedback(symptomTask, "skip", period, origIdx)
                    pendingSymptomFeedback = null
                }
            )
        }
    }
}

@Composable
fun PlanScreenHeader(onNavigateBack: () -> Unit = {}) {
    val colors = ZivaaTheme.colors
    val typography = ZivaaTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.ink,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = "Your Day",
                style = typography.eyebrow,
                color = colors.eyebrow
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.surfaceCard)
                .border(1.dp, colors.lineStrong, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notifications",
                tint = colors.ink,
                modifier = Modifier.size(20.dp)
            )
            // Notification dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colors.clay)
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
            )
        }
    }
}

@Composable
fun PlanScreenHero(
    summary: String = "",
    dateString: String = "",
    completedCount: Int = 0,
    totalCount: Int = 0
) {
    val colors = ZivaaTheme.colors
    val typography = ZivaaTheme.typography

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.sage)
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = if (dateString.isNotBlank()) "Plan • $dateString" else "Today's Plan",
                style = typography.eyebrow,
                color = colors.eyebrow
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = summary.ifEmpty { "A steady, gentle plan\nfor the day." },
                style = typography.bodyMedium.copy(
                    fontSize = 16.5.sp,
                    lineHeight = (16.5 * 1.45).sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic
                ),
                color = colors.sageInk
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (completedCount == totalCount && totalCount > 0) "All done for today! Well done." else if (completedCount > 0) "$completedCount completed so far." else "A fresh start — $totalCount small things today.",
                    style = typography.bodyMedium,
                    color = colors.sageInk
                )
                Text(
                    text = "$completedCount / $totalCount",
                    style = typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = colors.sageInk
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            val progressFraction = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(colors.ink.copy(alpha = 0.15f))
            ) {
                if (progressFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.sageInk)
                    )
                }
            }
        }
    }
}

@Composable
fun PlanSectionHeader(
    title: String,
    timeRange: String,
    progress: String,
    showNowTag: Boolean = false,
    modifier: Modifier = Modifier.padding(horizontal = 24.dp)
) {
    val colors = ZivaaTheme.colors
    val typography = ZivaaTheme.typography

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = typography.titleLarge,
                    color = colors.ink
                )
                if (showNowTag) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.clay)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(colors.sageInk)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "NOW",
                                style = typography.meta,
                                color = colors.sageInk
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeRange,
                style = typography.eyebrow,
                color = colors.eyebrow
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(colors.muted)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = progress,
                style = typography.meta,
                color = colors.ink
            )
        }
    }
}

@Composable
fun PlanTaskCard(
    tag: String,
    title: String,
    description: String?,
    icon: ImageVector,
    iconBgColor: androidx.compose.ui.graphics.Color,
    isCompleted: Boolean,
    onToggleCompletion: () -> Unit,
    actionText: String?,
    actionIcon: ImageVector?,
    actionBgColor: androidx.compose.ui.graphics.Color,
    actionTextColor: androidx.compose.ui.graphics.Color,
    isInsideTint: Boolean = false,
    isEditable: Boolean = true,
    onActionClick: (() -> Unit)? = null
) {
    val colors = ZivaaTheme.colors
    val typography = ZivaaTheme.typography
    
    val baseInsideModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(colors.bgElev)
        
    val baseOutsideModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .clip(RoundedCornerShape(24.dp))
        .background(colors.bgElev)
        .border(1.dp, colors.line, RoundedCornerShape(24.dp))

    val cardModifier = if (isInsideTint) {
        if (isEditable) baseInsideModifier.clickable { onToggleCompletion() } else baseInsideModifier
    } else {
        if (isEditable) baseOutsideModifier.clickable { onToggleCompletion() } else baseOutsideModifier
    }

    Box(modifier = cardModifier.padding(20.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Left Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.sageInk,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Middle Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tag,
                        style = typography.eyebrow,
                        color = colors.eyebrow
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        style = typography.leadParagraph,
                        color = colors.ink
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Right Checkbox
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(1.dp, if (isCompleted) colors.leaf else if (!isEditable) colors.line else colors.lineStrong, CircleShape)
                        .background(if (isCompleted) colors.leaf else if (!isEditable) colors.line else colors.bg),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = colors.bg,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (description != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = description,
                    style = typography.bodyMedium,
                    color = colors.inkSoft,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (actionText != null) {
                Spacer(modifier = Modifier.height(16.dp))
                val isAccent = actionBgColor != colors.bg && actionBgColor != Color.Transparent
                val actionBoxModifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isAccent) actionBgColor else colors.bg)
                    .border(
                        1.dp, 
                        if (isAccent) Color.Transparent else colors.lineStrong, 
                        RoundedCornerShape(24.dp)
                    )
                    .let { mod ->
                        if (onActionClick != null) mod.clickable { onActionClick() } else mod
                    }
                    .padding(horizontal = 4.dp, vertical = 4.dp)

                Box(
                    modifier = actionBoxModifier
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        if (actionIcon != null) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isAccent) Color.White.copy(alpha = 0.22f) else colors.leaf),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = actionIcon,
                                    contentDescription = null,
                                    tint = if (isAccent) Color.White else colors.sageInk,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(
                            text = actionText,
                            style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isAccent) Color.White else colors.ink
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = if (isAccent) Color.White.copy(alpha = 0.8f) else colors.inkMute,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun WeeklyDaySelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    progressMap: Map<LocalDate, Pair<Int, Int>> = emptyMap()
) {
    val colors = ZivaaTheme.colors
    // Generate dates for the current week (Sunday to Saturday)
    val today = LocalDate.now()
    val startOfWeek = today.minusDays((today.dayOfWeek.value % 7).toLong()) // Sunday
    val weekDates = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp) // Added 20dp padding here so it aligns
    ) {
        items(weekDates) { date ->
            val isSelected = date == selectedDate
            val isToday = date == today
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) colors.ink else Color.Transparent)
                    .clickable { onDateSelected(date) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) colors.bg else colors.inkSoft
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) colors.bg else colors.ink
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                val progress = progressMap[date]
                if (progress != null && progress.second > 0) {
                    val progressRatio = progress.first.toFloat() / progress.second.toFloat()
                    CircularProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier.size(16.dp),
                        color = if (isSelected) colors.bg else colors.leaf,
                        trackColor = if (isSelected) colors.bg.copy(alpha = 0.3f) else colors.lineStrong,
                        strokeWidth = 2.dp,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                } else if (isToday) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isSelected) colors.bg else colors.amber)
                    )
                } else {
                    Spacer(modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
