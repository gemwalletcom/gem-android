package com.gemwallet.android.features.swap.models

import androidx.compose.runtime.Stable
import com.wallet.core.primitives.Asset

enum class SwapItemType {
    Pay,
    Receive,
}

@Stable
class SwapItemState(
    val type: SwapItemType,
    val asset: Asset,
    val equivalentValue: String,
    val assetBalanceValue: String,
    val assetBalanceLabel: String,
    val calculating: Boolean = false,
)