package com.zivaa.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ZivaaTagStyle(
    val defaultText: String, 
    val bgColor: Color, 
    val contentColor: Color = Color.White, 
    val hasDot: Boolean = false
) {
    STEADY("STEADY ALL DAY", Color(0xFF3A554A), hasDot = true),
    WARNING("WORTH A LOOK", Color(0xFFC98A3A)),
    SUCCESS("LOOKING GOOD", Color(0xFF2B4D3E)),
    CRITICAL("NEEDS ATTENTION", Color(0xFFC56B5A))
}

@Composable
fun ZivaaTag(
    style: ZivaaTagStyle,
    modifier: Modifier = Modifier,
    customText: String? = null
) {
    val text = customText ?: style.defaultText
    
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(style.bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (style.hasDot) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5F1DB)) // Light green/white dot
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }
        
        Text(
            text = text,
            color = style.contentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )
    }
}
