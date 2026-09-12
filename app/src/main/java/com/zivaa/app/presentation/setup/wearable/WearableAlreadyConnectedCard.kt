package com.zivaa.app.presentation.setup.wearable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.setup.ZivaaButton
import com.zivaa.app.presentation.setup.ZivaaHeader
import com.zivaa.app.presentation.setup.ZivaaTopBar
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun WearableAlreadyConnectedCard(
    brand: WearableBrand,
    bpm: Int?,
    steps: Long?,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        ZivaaTopBar(stepNo = 4, totalSteps = 6, onBack = onBack)

        ZivaaHeader(
            label = "Device Ready",
            title = "Already connected!",
            subtitle = "Great news! Your ${brand.displayName} is already syncing with Google Health Connect."
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Hero Celebration Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = ZivaaTheme.colors.sage.copy(alpha = 0.12f),
            border = BorderStroke(1.5.dp, ZivaaTheme.colors.sage)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = ZivaaTheme.colors.sage
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = null,
                            tint = ZivaaTheme.colors.bgElev,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = brand.displayName,
                    style = ZivaaTheme.typography.cardTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    color = ZivaaTheme.colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Syncing smoothly via Google Health Connect",
                    style = ZivaaTheme.typography.bodyMedium,
                    color = ZivaaTheme.colors.inkSoft
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Stats preview row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (bpm != null && bpm > 0) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = ZivaaTheme.colors.bgElev
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = null,
                                    tint = ZivaaTheme.colors.rose,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "$bpm bpm",
                                        style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = ZivaaTheme.colors.ink
                                    )
                                    Text(
                                        text = "Recent pulse",
                                        style = ZivaaTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = ZivaaTheme.colors.inkSoft
                                    )
                                }
                            }
                        }
                    }

                    if (steps != null && steps > 0) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = ZivaaTheme.colors.bgElev
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DirectionsWalk,
                                    contentDescription = null,
                                    tint = ZivaaTheme.colors.sage,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "$steps",
                                        style = ZivaaTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = ZivaaTheme.colors.ink
                                    )
                                    Text(
                                        text = "Steps today",
                                        style = ZivaaTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = ZivaaTheme.colors.inkSoft
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        ZivaaButton(
            text = "Continue to Daily Rhythm",
            onClick = onContinue,
            modifier = Modifier.navigationBarsPadding()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
