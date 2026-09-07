package com.zivaa.app.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zivaa.app.presentation.profile.theme.ProfileTheme
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun StatsCard(
    morningReports: Int = 0,
    homeVisits: Int = 0,
    doctorCalls: Int = 0,
    sosResolved: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Zivaa So Far",
            style = ProfileTheme.typography.sectionHeader
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ProfileTheme.colors.cardBackground, RoundedCornerShape(24.dp))
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(number = morningReports.toString(), label = "morning\nreports", modifier = Modifier.weight(1f))
            VerticalDivider(color = ProfileTheme.colors.divider, modifier = Modifier.height(48.dp))
            StatItem(number = homeVisits.toString(), label = "home visits", modifier = Modifier.weight(1f))
            VerticalDivider(color = ProfileTheme.colors.divider, modifier = Modifier.height(48.dp))
            StatItem(number = doctorCalls.toString(), label = "doctor calls", modifier = Modifier.weight(1f))
            VerticalDivider(color = ProfileTheme.colors.divider, modifier = Modifier.height(48.dp))
            StatItem(number = sosResolved.toString(), label = "SOS resolved", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatItem(number: String, label: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = number,
            style = ProfileTheme.typography.statNumber
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = ProfileTheme.typography.statLabel,
            textAlign = TextAlign.Center
        )
    }
}
