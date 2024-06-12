package com.gemwallet.android.features.amount.model

import com.gemwallet.android.features.recipient.models.InputCurrency
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.TransactionType

sealed interface AmountScreenState {
    data object Loading : AmountScreenState

    class Loaded(
        val loading: Boolean,
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
        val inputCurrency: InputCurrency,
    ) : AmountScreenState

    data object Fatal : AmountScreenState
}