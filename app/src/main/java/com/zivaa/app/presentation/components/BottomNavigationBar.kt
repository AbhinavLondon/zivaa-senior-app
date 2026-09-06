package com.zivaa.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.ui.theme.ZivaaTheme

@Composable
fun BottomNavigationBar(
    selectedTab: String,
    modifier: Modifier = Modifier,
    onTabSelected: (String) -> Unit
) {
    Surface(
        modifier = modifier,
        color = ZivaaTheme.colors.bgElev,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(percent = 50),
        border = BorderStroke(0.5.dp, ZivaaTheme.colors.line),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple("Today", "home", Icons.Default.Home),
                Triple("Care", "care", Icons.Default.AutoAwesome),
                Triple("Health", "health", Icons.Default.FavoriteBorder),
                Triple("Wellness", "wellness", Icons.Default.Spa)
            )
            
            tabs.forEach { (label, id, icon) ->
                val isActive = selectedTab == id
                val tintColor = if (isActive) ZivaaTheme.colors.sage else ZivaaTheme.colors.inkMute
                
                Column(
                    modifier = Modifier
                        .clickable { onTabSelected(id) }
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tintColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        style = ZivaaTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = tintColor
                    )
                }
            }
        }
    }
}
