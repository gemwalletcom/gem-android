package com.gemwallet.android.ui.components.titles

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ext.getAddressEllipsisText
import com.gemwallet.android.ui.R
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType

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
        TransactionType.AssetActivation -> TODO()
        TransactionType.TransferNFT -> TODO()
        TransactionType.SmartContractCall -> TODO()
    }
}

@Composable
fun TransactionType.getTransactionTitle(direction: TransactionDirection, state: TransactionState, assetSymbol: String): String {
    return when (this) {
        TransactionType.StakeDelegate,
        TransactionType.StakeUndelegate,
        TransactionType.StakeRewards,
        TransactionType.StakeRedelegate,
        TransactionType.StakeWithdraw,
        TransactionType.Transfer,
        TransactionType.Swap -> stringResource(getTitle(direction, state))
        TransactionType.TokenApproval -> stringResource(id = R.string.transfer_approve_title)
        TransactionType.AssetActivation -> TODO()
        TransactionType.TransferNFT -> TODO()
        TransactionType.SmartContractCall -> TODO()
    }
}

fun TransactionType.getValue(direction: TransactionDirection, value: String): String {
    return when (this) {
        TransactionType.StakeUndelegate,
        TransactionType.StakeRewards,
        TransactionType.StakeRedelegate,
        TransactionType.StakeWithdraw -> value
        TransactionType.StakeDelegate -> value
        TransactionType.Transfer,
        TransactionType.Swap -> when (direction) {
            TransactionDirection.SelfTransfer,
            TransactionDirection.Outgoing -> "-${value}"
            TransactionDirection.Incoming -> "+${value}"
        }
        TransactionType.TokenApproval -> ""
        TransactionType.AssetActivation -> TODO()
        TransactionType.TransferNFT -> TODO()
        TransactionType.SmartContractCall -> TODO()
    }
}

@Composable
fun TransactionType.getAddress(direction: TransactionDirection, from: String, to: String): String {
    return when (this) {
        TransactionType.Transfer -> when (direction) {
            TransactionDirection.SelfTransfer,
            TransactionDirection.Outgoing -> "${stringResource(id = R.string.transfer_to)} ${to.getAddressEllipsisText()}"
            TransactionDirection.Incoming -> "${stringResource(id = R.string.transfer_from)} ${from.getAddressEllipsisText()}"
        }
        TransactionType.Swap,
        TransactionType.TokenApproval,
        TransactionType.StakeDelegate,
        TransactionType.StakeUndelegate,
        TransactionType.StakeRedelegate,
        TransactionType.StakeWithdraw,
        TransactionType.StakeRewards -> ""

        TransactionType.AssetActivation -> TODO()
        TransactionType.TransferNFT -> TODO()
        TransactionType.SmartContractCall -> TODO()
    }
}