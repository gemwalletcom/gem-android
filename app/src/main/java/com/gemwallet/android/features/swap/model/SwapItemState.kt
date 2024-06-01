package com.gemwallet.android.features.swap.model

import androidx.compose.runtime.Stable
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType

enum class SwapItemType {
    Pay,
    Receive,
}

@Stable
class SwapItemState(
    val type: SwapItemType,
    val assetId: AssetId,
    val assetIcon: String,
    val assetSymbol: String,
    val assetType: AssetType,
    val equivalentValue: String,
    val assetBalanceValue: String,
    val assetBalanceLabel: String,
    val calculating: Boolean = false,
)