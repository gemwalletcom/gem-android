package com.gemwallet.android.features.confirm.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.feeUnitType
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.TxSpeed
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.designsystem.trailingIcon16

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTxSpeed(
    currentSpeed: TxSpeed,
    fee: List<Fee>,
    onSelect: (TxSpeed) -> Unit,
    onCancel: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onCancel,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column {
            fee.forEach { item ->
                TxSpeedView(item, item.speed == currentSpeed) { onSelect(item.speed) }
            }
        }
    }
}

@Composable
private fun TxSpeedView(fee: Fee, isSelected: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(padding16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    modifier = Modifier.size(trailingIcon16),
                    imageVector = Icons.Outlined.Done,
                    contentDescription = ""
                )
            } else {
                Box(modifier = Modifier.size(trailingIcon16))
            }
            Spacer8()
            Text(
                modifier = Modifier.weight(1f),
                text = when (fee.speed) {
                    TxSpeed.Fast -> "\uD83D\uDE80  ${stringResource(R.string.fee_rates_fast)}"
                    TxSpeed.Normal -> "\uD83D\uDC8E  ${stringResource(R.string.fee_rates_normal)}"
                    TxSpeed.Slow -> "\uD83D\uDC22  ${stringResource(R.string.fee_rates_slow)}"
                },
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
            val feeUnitType = fee.feeAssetId.chain.feeUnitType()
            Text(
                text = "${(fee as? GasFee)?.maxGasPrice?.toString() ?: fee.feeAssetId.chain.asset().format(Crypto(fee.amount))} $feeUnitType",
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        HorizontalDivider(modifier = Modifier, thickness = 0.4.dp)
    }
}