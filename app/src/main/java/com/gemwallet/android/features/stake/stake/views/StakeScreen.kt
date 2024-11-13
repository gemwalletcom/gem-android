package com.gemwallet.android.features.stake.stake.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.stake.stake.model.StakeUIState
import com.gemwallet.android.features.stake.stake.viewmodels.StakeViewModel
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.wallet.core.primitives.AssetId

@Composable
fun StakeScreen(
    assetId: AssetId,
    amountAction: AmountTransactionAction,
    onConfirm: (ConfirmParams) -> Unit,
    onDelegation: (String, String) -> Unit,
    onCancel: () -> Unit,
    viewModel: StakeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(assetId.toIdentifier()) {

        viewModel.init(assetId)

        onDispose {  }
    }
    
    when (uiState) {
        is StakeUIState.FatalError -> FatalStateScene(
            title = (uiState as StakeUIState.FatalError).title,
            message = "Stake error",
            onCancel = onCancel
        )
        StakeUIState.Loading -> LoadingScene(
            title = stringResource(id = R.string.wallet_stake),
            onCancel = onCancel,
        )
        is StakeUIState.Loaded -> StakeScene(
            uiState = uiState as StakeUIState.Loaded,
            onRefresh = viewModel::onRefresh,
            amountAction = amountAction,
            onConfirm = { viewModel.onRewards(onConfirm) },
            onDelegation = onDelegation,
            onCancel = onCancel
        )
    }
}