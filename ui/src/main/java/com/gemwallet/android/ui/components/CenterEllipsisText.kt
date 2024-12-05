package com.gemwallet.android.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun CenterEllipsisText(
    text: String,
    modifier: Modifier = Modifier,
    maxLength: Int = 20,
    color: Color,
    style: TextStyle,
) {
    val displayText = if (text.length > maxLength) {
        val halfLength = (maxLength - 3) / 2
        "${text.take(halfLength)}...${text.takeLast(halfLength)}"
    } else {
        text
    }

    Text(
        text = displayText,
        modifier = modifier,
        maxLines = 1,
        color = color,
        style = style
    )
}