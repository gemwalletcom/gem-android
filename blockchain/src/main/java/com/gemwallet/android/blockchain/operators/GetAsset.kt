package com.gemwallet.android.blockchain.operators

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId

fun interface GetAsset {
    suspend fun getAsset(assetId: AssetId): Asset?
}