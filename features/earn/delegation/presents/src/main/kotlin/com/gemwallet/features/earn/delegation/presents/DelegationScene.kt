package com.gemwallet.features.earn.delegation.presents

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_head.AmountListHead
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.property.PropertyAssetBalanceItem
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.RewardsInfoUIModel
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.gemwallet.android.ui.models.actions.ConfirmTransactionAction
import com.gemwallet.features.earn.delegation.models.DelegationActions
import com.gemwallet.features.earn.delegation.models.DelegationBalances
import com.gemwallet.features.earn.delegation.models.DelegationProperty
import com.gemwallet.features.earn.delegation.presents.components.DelegationState
import com.gemwallet.features.earn.delegation.presents.components.StakeApr
import com.gemwallet.features.earn.delegation.presents.components.TransactionStatus
import com.gemwallet.features.earn.delegation.viewmodels.DelegationViewModel

@Composable
fun DelegationScene(
    onAmount: AmountTransactionAction,
    onConfirm: ConfirmTransactionAction,
    onCancel: () -> Unit,
    viewModel: DelegationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val delegationInfo by viewModel.delegationInfo.collectAsStateWithLifecycle()
    val properties by viewModel.properties.collectAsStateWithLifecycle()
    val balances by viewModel.balances.collectAsStateWithLifecycle()
    val actions by viewModel.actions.collectAsStateWithLifecycle()

    if (uiState == null) {
        LoadingScene(title = stringResource(id = R.string.transfer_stake_title), onCancel = onCancel)
        return
    }
    Scene(
        title = stringResource(R.string.transfer_stake_title),
        onClose = onCancel,
    ) {
        LazyColumn {
            delegationInfo?.let { info ->
                item {
                    AmountListHead(
                        amount = info.cryptoFormatted,
                        equivalent = info.fiatFormatted,
                        icon = info.iconUrl,
                    )
                }
            }
            itemsPositioned(properties) { position, item ->
                when (item) {
                    is DelegationProperty.Apr -> StakeApr(item.data, position)
                    is DelegationProperty.Name -> PropertyItem(R.string.stake_validator, item.data, listPosition = position)
                    is DelegationProperty.State -> DelegationState(
                        item.state,
                        item.availableIn,
                        position
                    )
                    is DelegationProperty.TransactionStatus -> TransactionStatus(
                        item.state,
                        item.isActive,
                        position
                    )
                }
            }

            itemsPositioned(balances) { position, item ->
                when (item) {
                    is RewardsInfoUIModel -> PropertyAssetBalanceItem(item, title = stringResource(R.string.stake_rewards), listPosition = position)
                    is DelegationBalances.Stake -> PropertyItem(R.string.transfer_stake_title, item.data, listPosition = position)
                }
            }

            if (actions.isNotEmpty()) {
                item { SubheaderItem(title = stringResource(id = R.string.common_manage)) }
            }
            itemsPositioned(actions) { position, item ->
                when (item) {
                    DelegationActions.RedelegateAction -> PropertyItem(R.string.transfer_redelegate_title, onClick = { viewModel.onRedelegate(onAmount) }, listPosition = position)
                    DelegationActions.StakeAction -> PropertyItem(R.string.transfer_stake_title, onClick = { viewModel.onStake(onAmount) }, listPosition = position)
                    DelegationActions.UnstakeAction -> PropertyItem(R.string.transfer_unstake_title, onClick = { viewModel.onUnstake(onAmount) }, listPosition = position)
                    DelegationActions.WithdrawalAction -> PropertyItem(R.string.transfer_withdraw_title, onClick = { viewModel.onWithdraw(onConfirm) }, listPosition = position)
                }
            }
        }
    }
}