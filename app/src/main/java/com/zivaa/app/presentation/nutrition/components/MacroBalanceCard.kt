package com.zivaa.app.presentation.nutrition.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.InstrumentSerif
import com.zivaa.app.ui.theme.LocalZivaaColors
import com.zivaa.app.ui.theme.LocalZivaaTypography

import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight

@Composable
fun MacroBalanceCard(
    proteinCurrent: Int, proteinTotal: Int,
    carbsCurrent: Int, carbsTotal: Int,
    fatCurrent: Int, fatTotal: Int,
    modifier: Modifier = Modifier
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colors.bgElev)
            .border(1.dp, colors.borderHairline, RoundedCornerShape(22.dp))
            .padding(22.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "The Balance",
                    style = typography.eyebrow,
                    color = colors.eyebrow
                )
                Text(
                    text = "Against your usual day",
                    style = typography.bodySmall,
                    color = colors.textMeta
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroRing(
                    current = proteinCurrent,
                    total = proteinTotal,
                    label = "Protein",
                    color = colors.leaf
                )
                MacroRing(
                    current = carbsCurrent,
                    total = carbsTotal,
                    label = "Carbs",
                    color = colors.amber
                )
                MacroRing(
                    current = fatCurrent,
                    total = fatTotal,
                    label = "Fat",
                    color = colors.clay
                )
            }
        }
    }
}

@Composable
private fun MacroRing(
    current: Int,
    total: Int,
    label: String,
    color: Color
) {
    val colors = LocalZivaaColors.current
    val typography = LocalZivaaTypography.current
    val progress = if (total > 0) current.toFloat() / total else 0f
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressRing(
            progress = progress,
            trackColor = colors.lineStrong,
            progressColor = color,
            strokeWidth = 6.5.dp,
            modifier = Modifier.size(72.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = current.toString(),
                    fontFamily = InstrumentSerif,
                    fontSize = 24.sp,
                    color = colors.textStrong
                )
                Text(
                    text = "OF ${total}G",
                    style = typography.meta.copy(
                        fontSize = 9.sp,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(0.05f, androidx.compose.ui.unit.TextUnitType.Em)
                    ),
                    color = colors.textMeta
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textStrong
        )
    }
}
