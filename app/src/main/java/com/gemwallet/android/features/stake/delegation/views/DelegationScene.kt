package com.gemwallet.android.features.stake.delegation.views

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.ext.redelegated
import com.gemwallet.android.features.stake.delegation.model.DelegationSceneState
import com.gemwallet.android.features.stake.delegation.viewmodels.DelegationViewModel
import com.gemwallet.android.features.stake.model.formatApr
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.SubheaderItem
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.gemwallet.android.ui.theme.pendingColor
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.StakeChain
import com.wallet.core.primitives.WalletType

@Composable
fun DelegationScene(
    validatorId: String,
    delegationId: String,
    onAmount: AmountTransactionAction,
    onCancel: () -> Unit,
    viewModel: DelegationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(validatorId, delegationId) {
        viewModel.init(validatorId, delegationId)

        onDispose {  }
    }

    when (uiState) {
        DelegationSceneState.Loading -> LoadingScene(title = stringResource(id = R.string.wallet_stake), onCancel = onCancel)
        is DelegationSceneState.Loaded -> {
            val state = uiState as DelegationSceneState.Loaded
            Scene(
                title = stringResource(R.string.wallet_stake),
                onClose = onCancel,
            ) {
                LazyColumn{
                    item {
                        Table(
                            items = listOf(
                                CellEntity(
                                    label = R.string.stake_validator,
                                    data = state.validator.name,
                                ),
                                CellEntity(
                                    label = stringResource(R.string.stake_apr, ""),
                                    data = state.validator.formatApr(),
                                    dataColor = when (state.validator.isActive) {
                                        true -> MaterialTheme.colorScheme.tertiary
                                        false -> MaterialTheme.colorScheme.secondary
                                    }
                                ),
                                CellEntity(
                                    label = R.string.transaction_status,
                                    data = stringResource(
                                        when (state.state) {
                                            DelegationState.Active -> when (state.validator.isActive) {
                                                true -> R.string.stake_active
                                                false -> R.string.stake_inactive
                                            }
                                            DelegationState.Pending -> R.string.stake_pending
                                            DelegationState.Undelegating -> R.string.transfer_unstake_title
                                            DelegationState.Inactive -> R.string.stake_inactive
                                            DelegationState.Activating -> R.string.stake_activating
                                            DelegationState.Deactivating -> R.string.stake_deactivating
                                            DelegationState.AwaitingWithdrawal -> com.gemwallet.android.localize.R.string.stake_awaiting_withdrawal
                                        }

                                    ),
                                    dataColor = when (state.state) {
                                        DelegationState.Active -> MaterialTheme.colorScheme.tertiary
                                        DelegationState.Activating,
                                        DelegationState.Deactivating,
                                        DelegationState.Pending,
                                        DelegationState.Undelegating -> pendingColor
                                        DelegationState.AwaitingWithdrawal,
                                        DelegationState.Inactive -> MaterialTheme.colorScheme.error
                                    },
                                ),
                                if ((state.state == DelegationState.Pending
                                    || state.state == DelegationState.Activating
                                    || state.state == DelegationState.Deactivating)
                                    && state.availableIn.isNotEmpty()
                                ) {
                                    val label = when (state.state) {
                                        DelegationState.Activating -> stringResource(id = R.string.stake_active_in)
                                        else -> stringResource(id = R.string.stake_available_in)
                                    }
                                    CellEntity(
                                        label = label,
                                        data = state.availableIn
                                    )
                                } else null
                            ).mapNotNull { it }
                        )
                    }

                    item { 
                        SubheaderItem(title = stringResource(id = R.string.asset_balances))
                        Table(items = state.balances)
                    }
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
    }
}

private fun LazyListScope.delegationActions(
    walletType: WalletType,
    stakeChain: StakeChain,
    state: DelegationState,
    onStake: () -> Unit,
    onUnstake: () -> Unit,
    onRedelegate: () -> Unit,
    onWithdraw: () -> Unit,
) {
    if (walletType == WalletType.view) {
        return
    }
    val cells = mutableListOf<CellEntity<Any>>()
    when (state) {
        DelegationState.Active -> {
            cells.add(
                CellEntity(
                    label = R.string.transfer_stake_title,
                    data = "",
                    action = onStake,
                ),
            )
            cells.add(
                CellEntity(
                    label = R.string.transfer_unstake_title,
                    data = "",
                    action = onUnstake,
                ),
            )
            if (stakeChain.redelegated()) {
                cells.add(
                    CellEntity(
                        label = R.string.transfer_redelegate_title,
                        data = "",
                        action = onRedelegate,
                    )
                )
            }
        }
        DelegationState.AwaitingWithdrawal -> cells.add(
            CellEntity(
                label = com.gemwallet.android.localize.R.string.transfer_withdraw_title,
                data = "",
                action = onWithdraw
            )
        )
        DelegationState.Pending,
        DelegationState.Undelegating,
        DelegationState.Inactive,
        DelegationState.Activating,
        DelegationState.Deactivating -> {}
    }
    if (cells.isNotEmpty()) {
        item {
            SubheaderItem(title = stringResource(id = R.string.common_manage))
            Table(items = cells)
        }
    }
}