package com.gemwallet.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

@Composable
fun Modifier.clickable(onClick: () -> Unit): Modifier {
    return Modifier
        .clip(MaterialTheme.shapes.medium)
        .then(this)
        .clickable(enabled = true, onClick = onClick)
}