package com.gemwallet.android.data.buy

import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.FiatQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuyRepository @Inject constructor(
    private val configRepository: ConfigRepository,
    private val remoteSource: GemApiClient,
) {

    fun getAvailable(): List<AssetId> {
        val assets = configRepository.getFiatAssets()
        return assets.assetIds.mapNotNull { it.toAssetId() }
    }

    suspend fun getQuote(
        asset: Asset,
        fiatCurrency: String,
        fiatAmount: Double,
        owner: String,
    ): Result<List<FiatQuote>> {
        val result = remoteSource.getQuote(asset.id.toIdentifier(), fiatAmount, fiatCurrency, owner)
        return result.mapCatching {
            if (it.quotes.isEmpty()) {
                throw Exception("Quotes not found")
            }
            it.quotes
        }
    }
}