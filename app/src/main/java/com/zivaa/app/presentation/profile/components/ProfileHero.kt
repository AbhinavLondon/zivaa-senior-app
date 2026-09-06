package com.zivaa.app.presentation.profile.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zivaa.app.presentation.profile.theme.ProfileTheme
import com.zivaa.app.ui.theme.ZivaaTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.BoxScope

@Composable
fun ProfileHero(
    name: String,
    initial: String,
    subtitle: String,
    profilePicUrl: String? = null,
    onAddPhotoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(ZivaaTheme.colors.amber),
                contentAlignment = Alignment.Center
            ) {
                if (profilePicUrl != null) {
                    AsyncImage(
                        model = profilePicUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    val lineColor = ProfileTheme.colors.accentGreen.copy(alpha = 0.15f)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val step = 15.dp.toPx()
                        var x = -size.height
                        while (x < size.width) {
                            drawLine(
                                color = lineColor,
                                start = Offset(x, 0f),
                                end = Offset(x + size.height, size.height),
                                strokeWidth = 4.dp.toPx()
                            )
                            x += step
                        }
                    }
                    Text(
                        text = initial,
                        fontFamily = com.zivaa.app.presentation.profile.theme.ManropeProfile,
                        fontSize = 52.sp,
                        color = ProfileTheme.colors.accentGreen,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            
            // Add Photo Button overlay
            IconButton(
                onClick = onAddPhotoClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
                    .size(36.dp)
                    .background(ProfileTheme.colors.accentGreen, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Add Photo",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = name,
            style = ProfileTheme.typography.heroName
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = subtitle,
            style = ProfileTheme.typography.meta,
            color = ProfileTheme.colors.textSecondary
        )
    }
}
