package com.zivaa.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.zivaa.app.R
import com.zivaa.app.ui.theme.ZivaaTheme
import androidx.compose.material.icons.filled.AutoAwesome

@Composable
fun FloatingCoachButton(onClick: () -> Unit, expanded: Boolean = true) {
    androidx.compose.material3.ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = Modifier.background(
            brush = Brush.linearGradient(
                colors = listOf(ZivaaTheme.colors.sage, ZivaaTheme.colors.leaf)
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
        ),
        containerColor = Color.Transparent,
        contentColor = ZivaaTheme.colors.bg,
        expanded = expanded,
        icon = {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.AutoAwesome,
                contentDescription = "AI Health Coach",
                modifier = Modifier.size(24.dp)
            )
        },
        text = {
            androidx.compose.material3.Text(
                text = "Ask Coach",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
            )
        },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
        elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    )
}
