package com.gemwallet.android.cases.swap

import com.wallet.core.primitives.AssetId
import uniffi.gemstone.SwapperAssetList

interface GetSwapSupported {
    fun getSwapSupportChains(assetId: AssetId): SwapperAssetList
}