package com.zivaa.app.presentation.mood.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.mood.theme.SahayakTheme
import com.zivaa.app.presentation.mood.theme.coloredShadow

import com.zivaa.app.data.remote.SupabasePatientCheckin
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MoodWeeklyHistory(
    modifier: Modifier = Modifier,
    checkins: List<SupabasePatientCheckin> = emptyList()
) {
    Column(modifier = modifier) {
        Text(
            text = "YOUR LAST SEVEN DAYS",
            style = SahayakTheme.typography.eyebrow,
            color = SahayakTheme.colors.inkMute,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 0.dp).padding(top = 24.dp, bottom = 10.dp)
        )

        Box(modifier = Modifier.padding(horizontal = 22.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .coloredShadow(
                        color = Color(0xFF234B3F),
                        alpha = 0.06f,
                        borderRadius = 22.dp,
                        shadowRadius = 24.dp,
                        offsetY = 8.dp
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(SahayakTheme.colors.bgElev)
                    .border(0.5.dp, SahayakTheme.colors.line, RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                // 7 days row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    val days = (6 downTo 0).map { offset ->
                        val date = LocalDate.now().minusDays(offset.toLong())
                        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val dayStr = date.format(DateTimeFormatter.ofPattern("E", Locale.ENGLISH)).take(1).uppercase()
                        
                        val dayCheckins = checkins.filter { it.date == dateStr }
                        val mostRecentCheckin = dayCheckins.maxByOrNull { it.date } 
                        val moodLabel = mostRecentCheckin?.mood_label
                        val moodIndex = if (moodLabel != null) listOf("Wonderful", "Good", "Okay", "Low", "Very low").indexOf(moodLabel).takeIf { it != -1 } else null
                        
                        Triple(dayStr, moodIndex, offset == 0)
                    }

                    days.forEach { (dayStr, moodIndex, isToday) ->
                        val color = when (moodIndex) {
                            0 -> SahayakTheme.colors.sage
                            1 -> SahayakTheme.colors.leaf
                            2 -> SahayakTheme.colors.amber
                            3 -> SahayakTheme.colors.clay
                            4 -> SahayakTheme.colors.rose
                            else -> SahayakTheme.colors.muted
                        }
                        
                        val bgTint = lerp(SahayakTheme.colors.bg, color, 0.13f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(bgTint, CircleShape)
                                    .then(
                                        if (isToday) Modifier.border(2.dp, color, CircleShape)
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (moodIndex != null) {
                                    FaceIcon(
                                        moodIndex = moodIndex,
                                        size = 24.dp,
                                        color = color
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = dayStr,
                                style = SahayakTheme.typography.meta.copy(
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                color = if (isToday) SahayakTheme.colors.ink else SahayakTheme.colors.inkMute
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SahayakTheme.colors.line))
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Text(
                    text = "Six bright days out of seven. Thursday dipped a little — and lifted by evening, after Karthik’s call.",
                    style = SahayakTheme.typography.bodySm.copy(lineHeight = 19.5.sp),
                    color = SahayakTheme.colors.inkSoft
                )
            }
        }
    }
}
