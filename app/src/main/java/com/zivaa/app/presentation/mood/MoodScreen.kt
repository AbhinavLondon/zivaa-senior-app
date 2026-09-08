package com.zivaa.app.presentation.mood

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.mood.components.MoodActionCard
import com.zivaa.app.presentation.mood.components.MoodCheckInCard
import com.zivaa.app.presentation.mood.components.MoodHeroCard
import com.zivaa.app.presentation.mood.components.MoodWeeklyHistory
import com.zivaa.app.presentation.mood.theme.SahayakTheme
import com.zivaa.app.ui.theme.toEyebrowTitleCase

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MoodScreen(
    viewModel: MoodViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCheckIn: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme()
) {
    val state by viewModel.state.collectAsState()
    val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    
    // Filter checkins for today
    val todayCheckins = state.checkins.filter { it.date == todayStr }
    val mostRecentToday = todayCheckins.maxByOrNull { it.date } // Or rely on list order since it's ordered by date.desc, created_at.desc
    
    val dateDisplay = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE dd MMM", Locale.ENGLISH))
    SahayakTheme(darkTheme = isDarkTheme) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(SahayakTheme.colors.bg)
                // Window insets padding for edge-to-edge
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Top Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 22.dp, top = 10.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SahayakTheme.colors.bgElev)
                        .border(0.5.dp, SahayakTheme.colors.line, CircleShape)
                        .clickable(onClick = onNavigateBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SahayakTheme.colors.ink,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Mood · ${dateDisplay.toEyebrowTitleCase()}",
                    style = SahayakTheme.typography.eyebrow
                )
            }

            // Scrolling Content
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                val todayMoodLabels = todayCheckins.mapNotNull { it.mood_label }
                MoodHeroCard(
                    modifier = Modifier.padding(horizontal = 22.dp).padding(top = 8.dp, bottom = 4.dp),
                    recentMoodLabel = mostRecentToday?.mood_label,
                    todayMoodLabels = todayMoodLabels,
                    onCheckInClick = onNavigateToCheckIn
                )
                
                if (mostRecentToday != null) {
                    MoodCheckInCard(
                        modifier = Modifier.fillMaxWidth(),
                        checkin = mostRecentToday
                    )
                }
                
                MoodWeeklyHistory(
                    modifier = Modifier.fillMaxWidth(),
                    checkins = state.checkins
                )
                
                MoodActionCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToCheckIn
                )

                // Closing Line
                Text(
                    text = "However the day feels, you can always tell us — that’s all a check-in is. We’ll ask again this evening.",
                    style = SahayakTheme.typography.bodySm.copy(fontSize = 12.5.sp, lineHeight = 19.375.sp),
                    color = SahayakTheme.colors.inkMute,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 22.dp)
                        .padding(bottom = 130.dp) // extra padding for floating bottom nav
                )
            }
        }
    }
}
