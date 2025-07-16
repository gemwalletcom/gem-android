package com.gemwallet.android.data.repositoreis.buy

import android.util.Log
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.Crypto
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.FiatQuote
import com.wallet.core.primitives.FiatQuoteType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuyRepository @Inject constructor(
    private val configStore: com.gemwallet.android.data.service.store.ConfigStore,
    private val gemApi: GemApiClient,
    private val assetsRepository: AssetsRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun sync() {
        try {
        val fiatVersion = gemApi.getConfig().versions.fiatOnRampAssets
        val currentVersion = configStore.getInt(ConfigKey.FiatAssetsVersion.string)
        if (currentVersion > 0 && currentVersion >= fiatVersion) return
        val availableToBuyIds = gemApi.getOnRampAssets()
        val availableToSellIds = gemApi.getOffRampAssets()
        val latestVersion = availableToBuyIds.version.toInt()
        assetsRepository.updateBuyAvailable(availableToBuyIds.assetIds)
        assetsRepository.updateSellAvailable(availableToSellIds.assetIds)
        configStore.putInt(ConfigKey.FiatAssetsVersion.string, latestVersion)
        } catch (err: Throwable) {
            Log.d("BUY_SYNC", "Error: ", err)
        }
    }

    suspend fun getQuotes(
        asset: Asset,
        type: FiatQuoteType,
        fiatCurrency: String,
        amount: Double,
        owner: String,
    ): List<FiatQuote> {
        return try {
            gemApi.getFiatQuotes(
                assetId = asset.id.toIdentifier(),
                type = type.string,
                fiatAmount = if (type == FiatQuoteType.Buy) amount else null,
                cryptoAmount = if (type == FiatQuoteType.Sell) Crypto(amount.toBigDecimal(), asset.decimals).atomicValue.toString() else null,
                currency = fiatCurrency,
                walletAddress = owner
            ).quotes
        } catch (err: Throwable) {
            throw Exception("Quotes not found", err)
        }
    }

    private enum class ConfigKey(val string: String) {
        FiatAssetsVersion("fiat-assets-version"),
        FiatAssets("fiat-assets"),
    }
}