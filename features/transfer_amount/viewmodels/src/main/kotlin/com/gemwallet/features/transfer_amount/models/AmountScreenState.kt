package com.gemwallet.features.transfer_amount.models

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.TransactionType

sealed interface AmountScreenState {
    data object Loading : AmountScreenState

    class Loaded(
        val error: AmountError,
        val txType: TransactionType,
        val assetId: AssetId,
        val assetSymbol: String,
        val assetIcon: String,
        val assetTitle: String,
        val assetType: AssetType,
        val availableAmount: String,
        val validator: DelegationValidator?,
        val equivalent: String,
    ) : AmountScreenState

    data object Fatal : AmountScreenState
}