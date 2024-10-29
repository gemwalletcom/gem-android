package com.gemwallet.android.data.repositories.buy

import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.FiatQuote
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuyRepository @Inject constructor(
    private val configRepository: ConfigRepository,
    private val remoteSource: GemApiClient,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
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
        return withContext(defaultDispatcher) {
            remoteSource.getQuote(asset.id.toIdentifier(), fiatAmount, fiatCurrency, owner).mapCatching {
                if (it.quotes.isEmpty()) {
                    throw Exception("Quotes not found")
                }
                it.quotes
            }
        }
    }
}