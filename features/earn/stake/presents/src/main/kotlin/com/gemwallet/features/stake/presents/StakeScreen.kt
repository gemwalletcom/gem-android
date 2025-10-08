package com.gemwallet.features.stake.presents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.gemwallet.features.stake.viewmodels.StakeViewModel

@Composable
fun StakeScreen(
    amountAction: AmountTransactionAction,
    onConfirm: (ConfirmParams) -> Unit,
    onDelegation: (String, String) -> Unit,
    onCancel: () -> Unit,
    viewModel: StakeViewModel = hiltViewModel()
) {
    val inSync by viewModel.isSync.collectAsStateWithLifecycle()
    val assetInfo by viewModel.assetInfo.collectAsStateWithLifecycle()
    val delegations by viewModel.delegations.collectAsStateWithLifecycle()
    val actions by viewModel.actions.collectAsStateWithLifecycle()

    if (assetInfo == null || (assetInfo?.stakeApr ?: 0.0) <= 0.0) {
        LoadingScene(
            title = stringResource(id = R.string.transfer_stake_title),
            onCancel = onCancel,
        )
    } else {
        StakeScene(
            inSync = inSync,
            assetInfo = assetInfo!!,
            delegations = delegations,
            actions = actions,
            onRefresh = viewModel::onRefresh,
            amountAction = amountAction,
            onConfirm = { viewModel.onRewards(onConfirm) },
            onDelegation = onDelegation,
            onCancel = onCancel
        )
    }
}