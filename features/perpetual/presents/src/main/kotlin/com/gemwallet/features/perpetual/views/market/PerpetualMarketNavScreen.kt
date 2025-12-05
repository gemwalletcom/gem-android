package com.gemwallet.features.perpetual.views.market

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.features.perpetual.viewmodels.PerpetualMarketViewModel

@Composable
fun PerpetualMarketNavScreen(
    onCancel: () -> Unit,
    viewModel: PerpetualMarketViewModel,
) {
    val perpetuals by viewModel.perpetuals.collectAsStateWithLifecycle()
    val positions by viewModel.positions.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()

    // TODO: Add swap to refresh
    PerpetualMarketScene(
        balance = balance ?: return,
        perpetuals = perpetuals,
        positions = positions,
        onClose = onCancel,
        onWithdraw = { },
        onDeposit = {},
    )
}