package com.gemwallet.android.cases.swap

import com.wallet.core.primitives.AssetId
import uniffi.gemstone.SwapperAssetList

interface GetSwapSupportedCase {
    fun getSwapSupportChains(assetId: AssetId): SwapperAssetList
}