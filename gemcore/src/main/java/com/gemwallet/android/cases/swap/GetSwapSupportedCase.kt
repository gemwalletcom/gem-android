package com.gemwallet.android.cases.swap

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import uniffi.gemstone.SwapAssetList

interface GetSwapSupportedCase {
    fun getSwapSupportChains(assetId: AssetId): SwapAssetList
}