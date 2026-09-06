package com.zivaa.app.presentation.nutrition.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DayItem(
    val date: LocalDate,
    val hasData: Boolean = true
)

@Composable
fun DaySelectorRow(
    days: List<DayItem>,
    selectedDate: LocalDate,
    onDaySelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        items(days, key = { it.date.toEpochDay() }) { day ->
            DayCard(
                day = day,
                isSelected = day.date == selectedDate,
                onClick = { onDaySelected(day.date) }
            )
        }
    }
}

@Composable
private fun DayCard(
    day: DayItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    
    val bgColor = if (isSelected) colors.sage else colors.bgElev
    val contentColor = if (isSelected) colors.sageInk else colors.textStrong
    val secondaryColor = if (isSelected) colors.sageInk else colors.textBody
    val dotColor = if (isSelected) colors.sageInk else colors.amber
    val borderColor = if (isSelected) colors.sage else colors.borderStrong
    
    val dayOfWeek = day.date.format(DateTimeFormatter.ofPattern("EEE")).uppercase()
    val dayOfMonth = day.date.dayOfMonth.toString()
    
    Column(
        modifier = Modifier
            .width(56.dp)
            .height(84.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayOfWeek,
            style = typography.meta,
            color = secondaryColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = dayOfMonth,
            style = typography.leadParagraph,
            color = contentColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        
        if (day.hasData) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        } else {
            Spacer(modifier = Modifier.size(4.dp))
        }
    }
}
