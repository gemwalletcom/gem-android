package com.gemwallet.android.features.stake.stake.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.stake.stake.viewmodels.StakeViewModel
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.models.actions.AmountTransactionAction

@Composable
fun StakeScreen(
    amountAction: AmountTransactionAction,
    onConfirm: (ConfirmParams) -> Unit,
    onDelegation: (String, String) -> Unit,
    onCancel: () -> Unit,
    viewModel: StakeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState == null || (uiState?.apr ?: 0.0) <= 0.0) {
        LoadingScene(
            title = stringResource(id = R.string.wallet_stake),
            onCancel = onCancel,
        )
    } else {
        StakeScene(
            uiState = uiState ?: return,
            onRefresh = viewModel::onRefresh,
            amountAction = amountAction,
            onConfirm = { viewModel.onRewards(onConfirm) },
            onDelegation = onDelegation,
            onCancel = onCancel
        )
    }
}