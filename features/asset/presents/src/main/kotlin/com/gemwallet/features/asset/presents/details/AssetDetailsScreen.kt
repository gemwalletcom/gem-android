package com.gemwallet.features.asset.presents.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.screen.FatalStateScene
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.gemwallet.features.asset.viewmodels.details.models.AssetInfoUIState
import com.gemwallet.features.asset.viewmodels.details.models.AssetStateError
import com.gemwallet.features.asset.viewmodels.details.viewmodels.AssetDetailsViewModel
import com.wallet.core.primitives.AssetId

@Composable
fun AssetDetailsScreen(
    onCancel: () -> Unit,
    onTransfer: AssetIdAction,
    onReceive: (AssetId) -> Unit,
    onBuy: (AssetId) -> Unit,
    onSwap: (AssetId, AssetId?) -> Unit,
    onTransaction: (txId: String) -> Unit,
    onChart: (AssetId) -> Unit,
    openNetwork: AssetIdAction,
    onStake: (AssetId) -> Unit,
    onConfirm: (ConfirmParams) -> Unit
) {
    val viewModel: AssetDetailsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val priceAlertEnabled by viewModel.priceAlertEnabled.collectAsStateWithLifecycle()
    val uiModel by viewModel.uiModel.collectAsStateWithLifecycle()
    val isOperationEnabled by viewModel.isOperationEnabled.collectAsStateWithLifecycle()

    when {
        uiState is AssetInfoUIState.Fatal -> FatalStateScene(
            title = "Asset",
            message = when ((uiState as AssetInfoUIState.Fatal).error) {
                AssetStateError.AssetNotFound -> "Asset not found"
            },
            onCancel = onCancel
        )
        uiState is AssetInfoUIState.Idle && uiModel != null -> AssetDetailsScene(
            uiState = uiModel ?: return,
            transactions = transactions,
            priceAlertEnabled = priceAlertEnabled,
            syncState = (uiState as AssetInfoUIState.Idle).sync,
            isOperationEnabled = isOperationEnabled,
            onRefresh = viewModel::refresh,
            onTransfer = onTransfer,
            onBuy = onBuy,
            onSwap = onSwap,
            onReceive = onReceive,
            onTransaction = onTransaction,
            onChart = onChart,
            openNetwork = openNetwork,
            onStake = onStake,
            onPriceAlert = viewModel::enablePriceAlert,
            onConfirm = onConfirm,
            onCancel = onCancel,
        )
        uiState is AssetInfoUIState.Loading || uiModel == null -> LoadingScene(stringResource(R.string.common_loading), onCancel)
    }
}

