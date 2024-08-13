package com.gemwallet.android.features.confirm.models

import com.gemwallet.android.model.AssetInfo
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.TransactionType

class AmountUIModel(
    val txType: TransactionType,
    val amount: String,
    val amountEquivalent: String,
    val fromAsset: AssetInfo,
    val toAsset: AssetInfo?,
    val fromAmount: String?,
    val toAmount: String?,
    val currency: Currency = Currency.USD,
)