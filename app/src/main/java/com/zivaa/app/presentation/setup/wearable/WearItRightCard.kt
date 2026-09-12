package com.zivaa.app.presentation.setup.wearable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.setup.ZivaaButton
import com.zivaa.app.presentation.setup.ZivaaHeader
import com.zivaa.app.presentation.setup.ZivaaTopBar
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun WearItRightCard(
    brand: WearableBrand,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        ZivaaTopBar(stepNo = 4, totalSteps = 6, onBack = onBack)

        ZivaaHeader(
            label = "Step 4 of 6 · Senior Health Tip",
            title = "Wear it right for accurate readings",
            subtitle = "Here are 3 simple habits to make sure your ${brand.displayName} measures your heart and sleep accurately."
        )

        Spacer(modifier = Modifier.height(20.dp))

        TipCard(
            number = "1",
            icon = Icons.Rounded.TouchApp,
            iconTint = ZivaaTheme.colors.sage,
            title = "Snug, but comfortable",
            description = "Wear your watch one finger's width above your wrist bone. A snug fit keeps the green sensor light against your skin for reliable heart rate."
        )

        Spacer(modifier = Modifier.height(12.dp))

        TipCard(
            number = "2",
            icon = Icons.Rounded.BatteryChargingFull,
            iconTint = ZivaaTheme.colors.amber,
            title = "Charge during your morning shower",
            description = "Pop your device on the charger for 20 minutes every morning while having tea or taking a bath. It will stay charged 24/7."
        )

        Spacer(modifier = Modifier.height(12.dp))

        TipCard(
            number = "3",
            icon = Icons.Rounded.NightsStay,
            iconTint = ZivaaTheme.colors.clay,
            title = "Sleep with it on",
            description = "Your nighttime sleep stages give the best insights into recovery and heart health. Keep it on as you sleep peacefully."
        )

        Spacer(modifier = Modifier.height(32.dp))

        ZivaaButton(
            text = "I'm Ready — Continue",
            onClick = onFinish
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TipCard(
    number: String,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ZivaaTheme.colors.bgElev,
        border = BorderStroke(1.dp, ZivaaTheme.colors.lineStrong)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = ZivaaTheme.colors.ink
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = ZivaaTheme.typography.bodyMedium,
                    color = ZivaaTheme.colors.inkSoft,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
