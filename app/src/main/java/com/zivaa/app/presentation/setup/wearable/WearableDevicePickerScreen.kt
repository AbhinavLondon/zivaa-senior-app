package com.zivaa.app.presentation.setup.wearable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.setup.ZivaaHeader
import com.zivaa.app.presentation.setup.ZivaaTopBar
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun WearableDevicePickerScreen(
    selectedBrand: WearableBrand,
    onBrandSelected: (WearableBrand) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        ZivaaTopBar(stepNo = 4, totalSteps = 6, onBack = onBack)

        ZivaaHeader(
            label = "Step 4 of 6 · Health Tracker",
            title = buildAnnotatedString {
                append("Which wearable do you ")
                withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = ZivaaTheme.colors.sage)) {
                    append("wear?")
                }
            },
            subtitle = "Choose your watch or smart ring. Zivaa will check if it is already syncing and guide you through any missing steps."
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Grid of Brands
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Option 1: Samsung Galaxy Watch (Popular)
            BrandCard(
                brand = WearableBrand.SAMSUNG,
                iconTint = ZivaaTheme.colors.sage,
                isSelected = selectedBrand == WearableBrand.SAMSUNG,
                isRecommended = true,
                onClick = { onBrandSelected(WearableBrand.SAMSUNG) }
            )

            // Option 2: Fitbit / Pixel Watch (Popular)
            BrandCard(
                brand = WearableBrand.FITBIT,
                iconTint = Color(0xFF00B0B9),
                isSelected = selectedBrand == WearableBrand.FITBIT,
                isRecommended = true,
                onClick = { onBrandSelected(WearableBrand.FITBIT) }
            )

            // Spacing between top popular watches and remaining list
            Spacer(modifier = Modifier.height(6.dp))

            BrandCard(
                brand = WearableBrand.OURA,
                iconTint = Color(0xFFD48B68),
                isSelected = selectedBrand == WearableBrand.OURA,
                onClick = { onBrandSelected(WearableBrand.OURA) }
            )

            BrandCard(
                brand = WearableBrand.ULTRAHUMAN,
                iconTint = Color(0xFFE05260),
                isSelected = selectedBrand == WearableBrand.ULTRAHUMAN,
                onClick = { onBrandSelected(WearableBrand.ULTRAHUMAN) }
            )

            BrandCard(
                brand = WearableBrand.GABIT,
                iconTint = Color(0xFF10B981),
                isSelected = selectedBrand == WearableBrand.GABIT,
                onClick = { onBrandSelected(WearableBrand.GABIT) }
            )

            BrandCard(
                brand = WearableBrand.GARMIN,
                iconTint = Color(0xFF007CC3),
                isSelected = selectedBrand == WearableBrand.GARMIN,
                onClick = { onBrandSelected(WearableBrand.GARMIN) }
            )

            BrandCard(
                brand = WearableBrand.INDIAN_BRANDS,
                iconTint = Color(0xFF8B5CF6),
                isSelected = selectedBrand == WearableBrand.INDIAN_BRANDS,
                onClick = { onBrandSelected(WearableBrand.INDIAN_BRANDS) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Phone step counter option
            BrandCard(
                brand = WearableBrand.NONE,
                iconTint = ZivaaTheme.colors.inkMute,
                isSelected = selectedBrand == WearableBrand.NONE,
                onClick = { onBrandSelected(WearableBrand.NONE) }
            )
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
private fun BrandCard(
    brand: WearableBrand,
    iconTint: Color,
    isSelected: Boolean,
    isRecommended: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) ZivaaTheme.colors.sage.copy(alpha = 0.08f) else ZivaaTheme.colors.bgElev,
        border = if (isSelected) BorderStroke(1.5.dp, ZivaaTheme.colors.sage) else BorderStroke(1.dp, ZivaaTheme.colors.lineStrong)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (isRecommended) {
                Row(
                    modifier = Modifier.padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1E3D34)
                    ) {
                        Text(
                            text = "POPULAR IN INDIA",
                            color = Color.White,
                            style = ZivaaTheme.typography.eyebrow.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            ),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = iconTint.copy(alpha = 0.14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        WearableBrandIcon(
                            brand = brand,
                            tint = iconTint,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = brand.displayName,
                        color = ZivaaTheme.colors.ink,
                        style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = brand.tagline,
                        color = ZivaaTheme.colors.inkSoft,
                        style = ZivaaTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = if (isSelected) ZivaaTheme.colors.sage else Color.Transparent,
                    border = if (!isSelected) BorderStroke(1.dp, ZivaaTheme.colors.lineStrong) else null
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Selected",
                                tint = ZivaaTheme.colors.bgElev,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
