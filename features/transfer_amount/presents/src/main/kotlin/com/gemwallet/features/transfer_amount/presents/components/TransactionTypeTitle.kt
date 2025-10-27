package com.gemwallet.features.transfer_amount.presents.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.wallet.core.primitives.TransactionType

@Composable
internal fun transactionTypeTitle(txType: TransactionType) = when (txType) {
    TransactionType.Transfer -> stringResource(id = R.string.transfer_send_title)
    TransactionType.StakeDelegate -> stringResource(id = R.string.transfer_stake_title)
    TransactionType.StakeUndelegate -> stringResource(id = R.string.transfer_unstake_title)
    TransactionType.StakeRedelegate -> stringResource(id = R.string.transfer_redelegate_title)
    TransactionType.StakeWithdraw -> stringResource(id = R.string.transfer_withdraw_title)
    TransactionType.TransferNFT -> stringResource(id = R.string.nft_collection)
    TransactionType.Swap -> stringResource(id = R.string.wallet_swap)
    TransactionType.TokenApproval -> stringResource(id = R.string.transfer_approve_title)
    TransactionType.StakeRewards -> stringResource(id = R.string.transfer_rewards_title)
    TransactionType.AssetActivation -> stringResource(id = R.string.transfer_activate_asset_title)
    TransactionType.SmartContractCall -> stringResource(id = R.string.transfer_amount_title)
    TransactionType.PerpetualOpenPosition -> stringResource(R.string.perpetual_position)
    TransactionType.PerpetualClosePosition -> stringResource(R.string.perpetual_close_position)
    TransactionType.StakeFreeze -> stringResource(R.string.transfer_freeze_title)
    TransactionType.StakeUnfreeze -> stringResource(R.string.transfer_unfreeze_title)
}