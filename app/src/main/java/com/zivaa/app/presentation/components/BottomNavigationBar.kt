package com.zivaa.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme

private data class NavTabItem(
    val label: String,
    val id: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

@Composable
fun BottomNavigationBar(
    selectedTab: String,
    modifier: Modifier = Modifier,
    onTabSelected: (String) -> Unit
) {
    val colors = ZivaaTheme.colors
    val isDark = colors.isDark

    // Active colors: luminous mint sage in dark mode, brand sage in light mode
    val activeColor = if (isDark) Color(0xFF8EE0B8) else colors.sage
    val activeIndicatorBg = if (isDark) Color(0xFF8EE0B8).copy(alpha = 0.18f) else colors.sage.copy(alpha = 0.12f)

    // Inactive colors: warm crisp stone/ivory in dark mode (contrast > 7:1), inkSoft in light mode
    val inactiveColor = if (isDark) Color(0xFFC5C2B8) else colors.inkSoft

    // Surface border & background: elevated contrast in dark mode so the pill is distinctly visible
    val surfaceBorder = if (isDark) Color(0x35EDE9DF) else colors.line
    val surfaceBg = if (isDark) Color(0xFF1E2226) else colors.bgElev

    Surface(
        modifier = modifier,
        color = surfaceBg,
        shape = RoundedCornerShape(percent = 50),
        border = BorderStroke(1.dp, surfaceBorder),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                NavTabItem("Today", "home", Icons.Filled.Home, Icons.Outlined.Home),
                NavTabItem("Care", "care", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
                NavTabItem("Health", "health", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
                NavTabItem("Wellness", "wellness", Icons.Filled.Spa, Icons.Outlined.Spa)
            )

            tabs.forEach { tab ->
                val isActive = selectedTab == tab.id
                val tabTint = if (isActive) activeColor else inactiveColor

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTabSelected(tab.id) }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isActive) activeIndicatorBg else Color.Transparent)
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isActive) tab.activeIcon else tab.inactiveIcon,
                            contentDescription = tab.label,
                            tint = tabTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        style = ZivaaTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = tabTint
                    )
                }
            }
        }
    }
}

