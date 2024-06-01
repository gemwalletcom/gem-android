package com.gemwallet.android.features.asset.chart.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun RowScope.PeriodButton(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.weight(0.16f).padding(bottom = 8.dp)) {
        if (isSelected) {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.09f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = title)
            }
        } else {
            TextButton(
                onClick = onClick,
                colors = ButtonDefaults.textButtonColors().copy(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text(text = title)
            }
        }
    }
}