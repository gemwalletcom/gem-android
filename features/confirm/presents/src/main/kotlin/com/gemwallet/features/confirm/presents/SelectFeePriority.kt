package com.gemwallet.features.confirm.presents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gemwallet.android.model.Fee
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.defaultPadding
import com.gemwallet.android.ui.theme.trailingIconSmall
import com.gemwallet.features.confirm.models.FeeRateUIModel
import com.wallet.core.primitives.FeePriority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectFeePriority(
    currentPriority: FeePriority,
    fee: List<Fee>,
    onSelect: (FeePriority) -> Unit,
    onCancel: () -> Unit,
) {
    ModalBottomSheet(onCancel) {
        Column {
            fee.forEach { item ->
                FeePriorityView(FeeRateUIModel(item), item.priority == currentPriority) { onSelect(item.priority) }
            }
        }
    }
}

@Composable
private fun FeePriorityView(fee: FeeRateUIModel, isSelected: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.defaultPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    modifier = Modifier.size(trailingIconSmall),
                    imageVector = Icons.Outlined.Done,
                    contentDescription = ""
                )
            } else {
                Box(modifier = Modifier.size(trailingIconSmall))
            }
            Spacer8()
            Text(
                modifier = Modifier.weight(1f),
                text = when (fee.priority) {
                    FeePriority.Fast -> "\uD83D\uDE80  ${stringResource(R.string.fee_rates_fast)}"
                    FeePriority.Normal -> "\uD83D\uDC8E  ${stringResource(R.string.fee_rates_normal)}"
                    FeePriority.Slow -> "\uD83D\uDC22  ${stringResource(R.string.fee_rates_slow)}"
                },
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = fee.price,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        HorizontalDivider(modifier = Modifier, thickness = 0.4.dp)
    }
}