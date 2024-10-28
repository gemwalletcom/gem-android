package com.gemwallet.android.ui.components.progress

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CircularProgressIndicator8(
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.circularColor,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size = 8.dp),
        strokeWidth = 1.dp,
        color = color,
    )
}

@Composable
fun CircularProgressIndicator10(
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.circularColor,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size = 10.dp),
        strokeWidth = 1.dp,
        color = color,
    )
}

@Composable
fun CircularProgressIndicator12(
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.circularColor,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size = 12.dp),
        strokeWidth = 1.dp,
        color = color,
    )
}

@Composable
fun CircularProgressIndicator14(
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.circularColor,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size = 14.dp),
        strokeWidth = 1.dp,
        color = color,
    )
}

@Composable
fun CircularProgressIndicator16(
    modifier: Modifier = Modifier,
    color: Color = ProgressIndicatorDefaults.circularColor,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size = 16.dp),
        strokeWidth = 1.dp,
        color = color,
    )
}