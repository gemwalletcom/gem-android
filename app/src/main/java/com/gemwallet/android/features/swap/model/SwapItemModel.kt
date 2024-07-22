package com.gemwallet.android.features.swap.model

import androidx.compose.runtime.Stable
import com.wallet.core.primitives.Asset

@Stable
class SwapItemModel(
    val asset: Asset,
    val assetBalanceValue: String,
    val assetBalanceLabel: String,
)