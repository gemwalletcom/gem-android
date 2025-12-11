package com.gemwallet.features.perpetual.views.market

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.features.perpetual.viewmodels.PerpetualMarketViewModel

@Composable
fun PerpetualMarketNavScreen(
    onCancel: () -> Unit,
    onOpenPerpetualDetails: (String) -> Unit,
    viewModel: PerpetualMarketViewModel = hiltViewModel(),
) {
    val sceneState by viewModel.sceneState.collectAsStateWithLifecycle()
    val unpinnedPerpetuals by viewModel.unpinnedPerpetuals.collectAsStateWithLifecycle()
    val pinnedPerpetuals by viewModel.pinnedPerpetuals.collectAsStateWithLifecycle()
    val positions by viewModel.positions.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()

    PerpetualMarketScene(
        sceneState = sceneState,
        balance = balance ?: return,
        unpinnedPerpetuals = unpinnedPerpetuals,
        pinnedPerpetuals = pinnedPerpetuals,
        positions = positions,
        onRefresh = viewModel::onRefresh,
        onPin = viewModel::onTogglePin,
        onClose = onCancel,
        onWithdraw = {},
        onDeposit = {},
        onClick = onOpenPerpetualDetails
    )
}