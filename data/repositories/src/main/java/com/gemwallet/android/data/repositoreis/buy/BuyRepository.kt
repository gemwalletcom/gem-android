package com.gemwallet.android.data.repositoreis.buy

import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.FiatQuote
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuyRepository @Inject constructor(
    private val configStore: com.gemwallet.android.data.service.store.ConfigStore,
    private val gemApi: GemApiClient,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun sync() {
        val fiatVersion = gemApi.getConfig().getOrNull()?.versions?.fiatAssets ?: return
        val currentVersion =
            configStore.getInt(ConfigKey.FiatAssetsVersion.string)
        if (currentVersion > 0 && currentVersion >= fiatVersion) return
        val availableIds = gemApi.getFiatAssets().getOrNull() ?: return
        val latestVersion = availableIds.version.toInt()
        configStore.putInt(
            ConfigKey.FiatAssetsVersion.string,
            latestVersion
        )
        configStore.putSet(
            ConfigKey.FiatAssets.string,
            availableIds.assetIds.toSet()
        )
    }

    fun getAvailable(): List<String> {
        val assets =
            configStore.getSet(ConfigKey.FiatAssets.string)
        return assets.toList()
    }

    suspend fun getQuote(
        asset: Asset,
        fiatCurrency: String,
        fiatAmount: Double,
        owner: String,
    ): Result<List<FiatQuote>> {
        return withContext(defaultDispatcher) {
            gemApi.getQuote(asset.id.toIdentifier(), fiatAmount, fiatCurrency, owner).mapCatching {
                if (it.quotes.isEmpty()) {
                    throw Exception("Quotes not found")
                }
                it.quotes
            }
        }
    }

    private enum class ConfigKey(val string: String) {
        FiatAssetsVersion("fiat-assets-version"),
        FiatAssets("fiat-assets"),
    }
}