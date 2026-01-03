package com.gemwallet.android.ui.components.list_item.transaction

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.domains.transaction.aggregates.TransactionDataAggregate
import com.gemwallet.android.ext.getAddressEllipsisText
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.theme.pendingColor
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType

@Composable
fun TransactionDataAggregate.getTitle(): String {
    return stringResource(type.getTitle(direction, state))
}

@Composable
fun TransactionDataAggregate.getBadgeText(): String = when (state) {
    TransactionState.Pending -> stringResource(id = R.string.transaction_status_pending)
    TransactionState.Confirmed -> ""
    TransactionState.Failed -> stringResource(id = R.string.transaction_status_failed)
    TransactionState.Reverted -> stringResource(id = R.string.transaction_status_reverted)
}

@Composable
fun TransactionDataAggregate.getBadgeColor(): Color = when (state) {
    TransactionState.Pending -> pendingColor
    TransactionState.Confirmed -> MaterialTheme.colorScheme.tertiary
    TransactionState.Reverted,
    TransactionState.Failed -> MaterialTheme.colorScheme.error
}

@Composable
fun TransactionDataAggregate.formatAddress(): String? = when (type) {
    TransactionType.TransferNFT,
    TransactionType.Transfer -> when (direction) {
        TransactionDirection.SelfTransfer,
        TransactionDirection.Outgoing -> "${stringResource(id = R.string.transfer_to)} $address"
        TransactionDirection.Incoming -> "${stringResource(id = R.string.transfer_from)} $address"
    }
    TransactionType.Swap,
    TransactionType.TokenApproval,
    TransactionType.StakeDelegate,
    TransactionType.StakeUndelegate,
    TransactionType.StakeRedelegate,
    TransactionType.StakeWithdraw,
    TransactionType.AssetActivation,
    TransactionType.StakeRewards,
    TransactionType.SmartContractCall,
    TransactionType.PerpetualOpenPosition,
    TransactionType.StakeFreeze,
    TransactionType.StakeUnfreeze,
    TransactionType.PerpetualClosePosition,
    TransactionType.PerpetualModifyPosition
        -> null
}

@Composable
fun TransactionDataAggregate.getValueColor(): Color = when (type) {
    TransactionType.Swap -> MaterialTheme.colorScheme.tertiary
    else -> when (direction) {
        TransactionDirection.SelfTransfer,
        TransactionDirection.Outgoing -> MaterialTheme.colorScheme.onSurface
        TransactionDirection.Incoming -> MaterialTheme.colorScheme.tertiary
    }
}

// TODO: Deprecating it
fun TransactionType.getTitle(direction: TransactionDirection? = null, state: TransactionState? = null): Int {
    return when (this) {
        TransactionType.StakeDelegate -> R.string.transfer_stake_title
        TransactionType.StakeUndelegate -> R.string.transfer_unstake_title
        TransactionType.StakeRedelegate -> R.string.transfer_redelegate_title
        TransactionType.StakeRewards -> R.string.transfer_rewards_title
        TransactionType.Transfer -> when (state) {
            TransactionState.Failed,
            TransactionState.Reverted,
            TransactionState.Pending -> R.string.transfer_title
            TransactionState.Confirmed -> when (direction) {
                TransactionDirection.Incoming -> R.string.transaction_title_received
                else -> R.string.transaction_title_sent
            }
            else -> R.string.transfer_send_title
        }

        TransactionType.Swap -> R.string.wallet_swap
        TransactionType.TokenApproval -> R.string.transfer_approve_title
        TransactionType.StakeWithdraw -> R.string.transfer_withdraw_title
        TransactionType.AssetActivation -> R.string.transfer_activate_asset_title
        TransactionType.TransferNFT -> R.string.transfer_title
        TransactionType.SmartContractCall -> R.string.transfer_smart_contract_title
        TransactionType.PerpetualOpenPosition -> R.string.perpetual_position
        TransactionType.PerpetualClosePosition -> R.string.perpetual_close_position
        TransactionType.StakeFreeze -> R.string.transfer_freeze_title
        TransactionType.StakeUnfreeze -> R.string.transfer_unfreeze_title
        TransactionType.PerpetualModifyPosition -> R.string.perpetual_modify
    }
}