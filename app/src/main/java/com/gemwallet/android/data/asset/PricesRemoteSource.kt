package com.gemwallet.android.data.asset

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetPrice

interface PricesRemoteSource {
    suspend fun loadPrices(currencyCode: String, assets: List<AssetId>): Result<List<AssetPrice>>
}