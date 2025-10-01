package com.gemwallet.features.swap.views.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.gemwallet.features.swap.viewmodels.models.SwapProperty
import com.wallet.core.primitives.Asset

@Composable
internal fun PriceImpactWarningDialog(
    isShowPriceImpactAlert: MutableState<Boolean>,
    priceImpact: SwapProperty.PriceImpact?,
    asset: Asset?,
    onContinue: () -> Unit,
) {
    if (!isShowPriceImpactAlert.value || priceImpact == null || asset == null) {
        return
    }

    val dismiss = fun () { isShowPriceImpactAlert.value = false }

    AlertDialog(
        onDismissRequest = dismiss,
        confirmButton = {
            Button(
                {
                    onContinue()
                    dismiss()
                }
            ) {
                Text(stringResource(R.string.common_continue))
            }
        },
        dismissButton = {
            Button(dismiss) {
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