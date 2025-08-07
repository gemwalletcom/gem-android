package com.gemwallet.features.swap.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.gemwallet.features.swap.viewmodels.models.PriceImpact
import com.wallet.core.primitives.Asset

@Composable
internal fun PriceImpactWarningDialog(
    priceImpact: PriceImpact?,
    asset: Asset?,
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
) {
    priceImpact ?: return
    asset ?: return

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onContinue) {
                Text(stringResource(R.string.common_continue))
            }
        },
        dismissButton = {
            Button(onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
        title = { Text(stringResource(R.string.swap_price_impact_warning_title)) },
        text = {
            Text(
                stringResource(
                    R.string.swap_price_impact_warning_description,
                    priceImpact.percentageFormatted,
                    asset.symbol,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    )
}