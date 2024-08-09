package com.gemwallet.android.features.confirm.models

import com.gemwallet.android.model.AssetInfo

class AmountUIModel(
    val amount: String,
    val amountEquivalent: String,
    val fromAsset: AssetInfo,
    val toAsset: AssetInfo?,
    val fromAmount: String?,
    val toAmount: String?,
)