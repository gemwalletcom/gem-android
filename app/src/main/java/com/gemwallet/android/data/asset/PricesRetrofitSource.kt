package com.gemwallet.android.data.asset

import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.AssetPricesRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PricesRetrofitSource @Inject constructor(
    private val rpcClient: GemApiClient,
) : PricesRemoteSource {

    override suspend fun loadPrices(currencyCode: String, assets: List<AssetId>): Result<List<AssetPrice>> {
        val request = AssetPricesRequest(
            currency = currencyCode,
            assetIds = assets.map { it.toIdentifier() },
        )
        val response = try {
            rpcClient.getTickers(request).body()
        } catch (err: Throwable) {
            null
        } ?: return Result.failure(Exception("Invalid response"))
        return Result.success(response.prices)
    }
}