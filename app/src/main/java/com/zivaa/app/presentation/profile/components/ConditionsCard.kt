package com.zivaa.app.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.zivaa.app.data.remote.ConditionRecord
import com.zivaa.app.presentation.profile.theme.ProfileTheme
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun ConditionsCard(
    conditions: List<ConditionRecord>,
    onAddClick: () -> Unit = {},
    onDeleteClick: (ConditionRecord) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "CONDITIONS",
            style = ProfileTheme.typography.sectionHeader
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ProfileTheme.colors.cardBackground, RoundedCornerShape(24.dp))
                .padding(vertical = 16.dp, horizontal = 24.dp)
        ) {
            conditions.forEach { condition ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ProfileTheme.colors.accentGreen)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = condition.conditionName,
                        style = ProfileTheme.typography.cardTitle,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onDeleteClick(condition) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete condition",
                            tint = ProfileTheme.colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                HorizontalDivider(
                    color = ProfileTheme.colors.divider,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddClick() }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "+ Add a condition",
                    style = ProfileTheme.typography.cardTitle,
                    color = ProfileTheme.colors.accentGreen
                )
            }
        }
    }
}
