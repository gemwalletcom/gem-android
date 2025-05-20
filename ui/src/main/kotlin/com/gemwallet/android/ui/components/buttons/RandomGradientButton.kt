package com.gemwallet.android.ui.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RandomGradientButton(
    modifier: Modifier = Modifier,
    cornerRadius: Float = 12f,
    borderWidth: Float = 3f,
    onClick: () -> Unit,
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2A32FF),
            Color(0xFF6CB8FF),
            Color(0xFFF213F6),
            Color(0xFFFFF963)
        ),
        start = Offset(0f, 0f),
        end = Offset(100f, 100f)
    )

    Box(
        modifier = modifier
            .size(38.dp)
            .background(Color.Transparent)
            .clip(RoundedCornerShape(cornerRadius.dp))
            .border(
                width = borderWidth.dp,
                brush = gradientBrush,
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🎲",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W400)
        )
    }
}