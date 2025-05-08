package com.gemwallet.android.features.stake.delegation.views

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.redelegated
import com.gemwallet.android.features.stake.delegation.model.DelegationSceneState
import com.gemwallet.android.features.stake.delegation.viewmodels.DelegationViewModel
import com.gemwallet.android.features.stake.model.formatApr
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.list_item.PropertyItem
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.gemwallet.android.ui.theme.pendingColor
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.StakeChain
import com.wallet.core.primitives.WalletType

@Composable
fun DelegationScene(
    onAmount: AmountTransactionAction,
    onCancel: () -> Unit,
    viewModel: DelegationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState == null) {
        LoadingScene(title = stringResource(id = R.string.wallet_stake), onCancel = onCancel)
        return
    }
    val state = uiState!!
    Scene(
        title = stringResource(R.string.wallet_stake),
        onClose = onCancel,
    ) {
        LazyColumn{
            validatorNameItem(state.validator.name)
            stakeApr(state.validator)
            transactionStatus(state)
            delegationState(state)
            delegationBalances(state.stakeBalance, state.rewardsBalance)
            delegationActions(
                walletType = state.walletType,
                stakeChain = state.stakeChain,
                state = state.state,
                onStake = { viewModel.onStake(onAmount) },
                onUnstake = { viewModel.onUnstake(onAmount) },
                onRedelegate = { viewModel.onRedelegate(onAmount) },
                onWithdraw = { viewModel.onWithdraw(onAmount) },
            )
        }
    }
}

private fun LazyListScope.delegationBalances(stakeBalance: String, rewardsBalance: String) {
    item {
        SubheaderItem(title = stringResource(id = R.string.asset_balances))
        PropertyItem(R.string.wallet_stake, stakeBalance)
        PropertyItem(R.string.stake_rewards, rewardsBalance)
    }
}

private fun LazyListScope.delegationState(state: DelegationSceneState) {
    if ((state.state == DelegationState.Pending
                || state.state == DelegationState.Activating
                || state.state == DelegationState.Deactivating)
        && state.availableIn.isNotEmpty()
    ) {
        item {
            PropertyItem(
                when (state.state) {
                    DelegationState.Activating -> R.string.stake_active_in
                    else -> R.string.stake_available_in
                },
                state.availableIn
            )
        }
    }
}

private fun LazyListScope.transactionStatus(state: DelegationSceneState) {
    item {
        PropertyItem(
            title = R.string.transaction_status,
            data = when (state.state) {
                DelegationState.Active -> when (state.validator.isActive) {
                    true -> R.string.stake_active
                    false -> R.string.stake_inactive
                }
                DelegationState.Pending -> R.string.stake_pending
                DelegationState.Undelegating -> R.string.transfer_unstake_title
                DelegationState.Inactive -> R.string.stake_inactive
                DelegationState.Activating -> R.string.stake_activating
                DelegationState.Deactivating -> R.string.stake_deactivating
                DelegationState.AwaitingWithdrawal -> R.string.stake_awaiting_withdrawal
            },
            dataColor = when (state.state) {
                DelegationState.Active -> MaterialTheme.colorScheme.tertiary
                DelegationState.Activating,
                DelegationState.Deactivating,
                DelegationState.Pending,
                DelegationState.Undelegating -> pendingColor
                DelegationState.AwaitingWithdrawal,
                DelegationState.Inactive -> MaterialTheme.colorScheme.error
            },
        )
    }
}

private fun LazyListScope.stakeApr(validator: DelegationValidator) {
    item {
        PropertyItem(
            stringResource(R.string.stake_apr, ""),
            validator.formatApr(),
            dataColor = when (validator.isActive) {
                true -> MaterialTheme.colorScheme.tertiary
                false -> MaterialTheme.colorScheme.secondary
            }
        )
    }
}

private fun LazyListScope.delegationActions(
    walletType: WalletType,
    state: DelegationState,
    stakeChain: StakeChain,
    onStake: () -> Unit,
    onUnstake: () -> Unit,
    onRedelegate: () -> Unit,
    onWithdraw: () -> Unit,
) {
    if (walletType == WalletType.view) {
        return
    }
    item {
        state.takeIf { it == DelegationState.Active }?.let {
            DelegationActiveAction(stakeChain, onStake, onUnstake, onRedelegate)
        }
        state.takeIf { it == DelegationState.AwaitingWithdrawal }?.let {
            DelegationWithdrawAction(onWithdraw)
        }
    }
}

@Composable
private fun LazyItemScope.DelegationActiveAction(
    stakeChain: StakeChain,
    onStake: () -> Unit,
    onUnstake: () -> Unit,
    onRedelegate: () -> Unit,
) {
    SubheaderItem(title = stringResource(id = R.string.common_manage))
    PropertyItem(R.string.transfer_stake_title, onClick = onStake)
    PropertyItem(R.string.transfer_unstake_title, onClick = onUnstake)
    if (stakeChain.redelegated()) {
        PropertyItem(R.string.transfer_redelegate_title, onClick = onRedelegate)
    }
}

@Composable
private fun LazyItemScope.DelegationWithdrawAction(onWithdraw: () -> Unit) {
    SubheaderItem(title = stringResource(id = R.string.common_manage))
    PropertyItem(R.string.transfer_withdraw_title, onClick = onWithdraw)
}

private fun LazyListScope.validatorNameItem(name: String) {
    item { PropertyItem(R.string.stake_validator, name) }
}