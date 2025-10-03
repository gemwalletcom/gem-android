package com.gemwallet.android.ui.components.list_item

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SubheaderItem(
    title: String,
    modifier: Modifier = Modifier,
    paddings: PaddingValues = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp)
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddings),
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary,
    )
}