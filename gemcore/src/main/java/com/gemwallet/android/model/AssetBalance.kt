package com.gemwallet.android.model

import com.wallet.core.primitives.AssetId

data class AssetBalance(
    val assetId: AssetId,
    val balance: Balance,
)
