package com.gemwallet.android.features.asset.chart.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.wallet.core.primitives.ChartPeriod

@Composable
internal fun PeriodsPanel(
    period: ChartPeriod,
    onSelect: (ChartPeriod) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChartPeriod.entries.forEach {
            val label = it.getLabel() ?: return@forEach
            PeriodButton(label, it == period) {
                onSelect(it)
            }
        }
    }
}

@Composable
private fun RowScope.PeriodButton(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier
        .weight(0.16f)
        .padding(bottom = 8.dp)) {
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
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = title)
            }
        }
    }
}

@Composable
private fun ChartPeriod.getLabel(): String? {
    val strId = when (this) {
        ChartPeriod.Hour -> R.string.charts_hour
        ChartPeriod.Day -> R.string.charts_day
        ChartPeriod.Week -> R.string.charts_week
        ChartPeriod.Month -> R.string.charts_month
        ChartPeriod.Year -> R.string.charts_year
        ChartPeriod.All -> R.string.charts_all
        ChartPeriod.Quarter -> return null
    }
    return stringResource(id = strId)
}