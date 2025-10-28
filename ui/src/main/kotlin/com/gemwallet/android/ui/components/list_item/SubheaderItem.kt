package com.gemwallet.android.ui.components.list_item

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.theme.paddingDefault

@Composable
fun SubheaderItem(
    title: String,
    modifier: Modifier = Modifier,
    paddings: PaddingValues = PaddingValues(start = paddingDefault, end = paddingDefault, top = paddingDefault, bottom = 0.dp)
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