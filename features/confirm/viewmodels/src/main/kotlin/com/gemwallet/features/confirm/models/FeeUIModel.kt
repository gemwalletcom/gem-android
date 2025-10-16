package com.gemwallet.features.confirm.models

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.FeePriority

sealed interface FeeUIModel {
    object Calculating : FeeUIModel

    object Error : FeeUIModel

    class FeeInfo(
        val cryptoAmount: String,
        val fiatAmount: String,
        val feeAsset: Asset,
        val priority: FeePriority,
    ) : FeeUIModel
}