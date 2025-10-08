package com.gemwallet.features.stake.models

import androidx.annotation.StringRes
import com.wallet.core.primitives.TransactionType

sealed class StakeAction(val transactionType: TransactionType, val data: String? = null) {
    object Stake : StakeAction(TransactionType.StakeDelegate)

    class Rewards(data: String) : StakeAction(TransactionType.StakeRewards, data)

    object Freeze : StakeAction(TransactionType.StakeFreeze)

    object Unfreeze: StakeAction(TransactionType.StakeUnfreeze)
}