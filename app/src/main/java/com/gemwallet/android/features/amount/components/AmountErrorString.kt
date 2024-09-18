package com.gemwallet.android.features.amount.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.R
import com.gemwallet.android.features.amount.models.AmountError

@Composable
fun amountErrorString(error: AmountError): String = when (error) {
    AmountError.None -> ""
    AmountError.IncorrectAmount -> stringResource(id = R.string.errors_unable_estimate_network_fee)
    AmountError.Required -> stringResource(
        id = R.string.common_required_field,
        stringResource(id = R.string.transfer_amount)
    )
    AmountError.Unavailable -> "Unavailable"
    is AmountError.InsufficientBalance -> stringResource(
        id = R.string.transfer_insufficient_balance,
        error.assetName
    )
    is AmountError.InsufficientFeeBalance -> stringResource(
        id = R.string.transfer_insufficient_network_fee_balance,
        error.assetName
    )
    AmountError.ZeroAmount -> "Zero amount"
    is AmountError.MinimumValue -> stringResource(
        id = R.string.transfer_minimum_amount,
        error.minimumValue
    )
    AmountError.IncorrectAddress -> stringResource(id = R.string.errors_invalid_address_name)
}