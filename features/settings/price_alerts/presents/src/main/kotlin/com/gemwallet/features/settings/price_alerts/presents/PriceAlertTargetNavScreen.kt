package com.gemwallet.features.settings.price_alerts.presents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.features.settings.price_alerts.viewmodels.PriceAlertTargetViewModel

@Composable
fun PriceAlertTargetNavScreen(
    onCancel: () -> Unit,
    viewModel: PriceAlertTargetViewModel = hiltViewModel(),
) {
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val currencyPrice by viewModel.currentPrice.collectAsStateWithLifecycle()
    val currencyPriceValue by viewModel.currentPriceValue.collectAsStateWithLifecycle()
    val type by viewModel.type.collectAsStateWithLifecycle()
    val direction by viewModel.direction.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    PriceAlertTargetScene(
        value = viewModel.value,
        type = type,
        direction = direction,
        currency = currency,
        currentPriceValue = currencyPriceValue,
        currentPriceFormatted = currencyPrice,
        error = error,
        onType = viewModel::onType,
        onDirection = viewModel::onDirection,
        onConfirm = {
            viewModel.onConfirm()
            onCancel()
        },
        onCancel = onCancel,
    )
}