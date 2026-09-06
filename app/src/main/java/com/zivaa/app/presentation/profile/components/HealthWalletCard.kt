package com.zivaa.app.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.profile.theme.ProfileTheme

@Composable
fun HealthWalletCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ProfileTheme.colors.cardBackground)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(ProfileTheme.colors.accentGreen, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountBalanceWallet,
                contentDescription = "Health Wallet",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Health Wallet",
                style = ProfileTheme.typography.cardTitle.copy(fontSize = 18.sp, color = ProfileTheme.colors.textPrimary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "20 documents · 5 medicines",
                style = ProfileTheme.typography.cardSubtitle.copy(fontSize = 14.sp, color = ProfileTheme.colors.textSecondary)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Open Health Wallet",
            tint = ProfileTheme.colors.textSecondary
        )
    }
}
