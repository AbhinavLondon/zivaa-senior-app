package com.zivaa.app.presentation.mood.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.mood.theme.SahayakTheme
import com.zivaa.app.presentation.mood.theme.coloredShadow
import androidx.compose.ui.graphics.lerp

import com.zivaa.app.data.remote.SupabasePatientCheckin

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun MoodCheckInCard(
    modifier: Modifier = Modifier,
    checkin: SupabasePatientCheckin
) {
    Column(modifier = modifier) {
        Text(
            text = "From This Morning's Check-In",
            style = SahayakTheme.typography.eyebrow,
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
            ) {
                // Feeling section
                if (checkin.emotions.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "Feeling",
                            style = SahayakTheme.typography.meta.copy(fontSize = 12.sp, letterSpacing = 0.07.em),
                            modifier = Modifier.width(64.dp).padding(top = 6.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val baseColor = getHeroBackgroundColor(checkin.mood_label)
                            val bgTint = lerp(SahayakTheme.colors.bg, baseColor, 0.12f)
                            val textColor = baseColor
                            
                            checkin.emotions.forEach { tag ->
                                Text(
                                    text = tag,
                                    style = SahayakTheme.typography.bodySm.copy(fontSize = 13.5.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                    color = textColor,
                                    modifier = Modifier
                                        .background(bgTint, RoundedCornerShape(999.dp))
                                        .padding(horizontal = 13.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Divider (only if both are present)
                if (checkin.emotions.isNotEmpty() && checkin.causes.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SahayakTheme.colors.line))
                }

                // Behind it section
                if (checkin.causes.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "Behind It",
                            style = SahayakTheme.typography.meta.copy(fontSize = 12.sp, letterSpacing = 0.07.em),
                            modifier = Modifier.width(64.dp).padding(top = 6.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val baseColor = getHeroBackgroundColor(checkin.mood_label)
                            val bgTint = lerp(SahayakTheme.colors.bg, baseColor, 0.13f)
                            val textColor = baseColor
                            
                            checkin.causes.forEach { tag ->
                                Text(
                                    text = tag,
                                    style = SahayakTheme.typography.bodySm.copy(fontSize = 13.5.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                    color = textColor,
                                    modifier = Modifier
                                        .background(bgTint, RoundedCornerShape(999.dp))
                                        .padding(horizontal = 13.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
