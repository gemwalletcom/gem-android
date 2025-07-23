package com.gemwallet.android.ui.models

import com.wallet.core.primitives.TransactionType

enum class TransactionTypeFilter(val types: List<TransactionType>) {
    Transfer(listOf(TransactionType.Transfer, TransactionType.TransferNFT)),
    Swap(listOf(TransactionType.TokenApproval, TransactionType.Swap)),
    Stake(
        listOf(
            TransactionType.StakeDelegate,
            TransactionType.StakeRedelegate,
            TransactionType.StakeUndelegate,
            TransactionType.StakeRewards,
            TransactionType.StakeWithdraw,
        ),
    ),
    SmartContract(listOf(TransactionType.SmartContractCall)),
    Other(listOf(TransactionType.AssetActivation))
}