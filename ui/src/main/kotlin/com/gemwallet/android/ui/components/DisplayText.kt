package com.gemwallet.android.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

@Composable
fun DisplayText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = text,
        overflow = TextOverflow.MiddleEllipsis,
        maxLines = 1,
        style = if ("✱✱✱✱✱✱" == text) {
            MaterialTheme.typography.headlineSmall.copy(lineHeight = 44.0.sp)
        } else {
            MaterialTheme.typography.displaySmall.copy(lineHeight = 44.0.sp)
        },
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )
}