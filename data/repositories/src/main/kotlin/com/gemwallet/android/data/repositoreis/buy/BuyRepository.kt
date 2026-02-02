package com.gemwallet.android.data.repositoreis.buy

import com.gemwallet.android.cases.device.GetDeviceIdOld
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.FiatQuote
import com.wallet.core.primitives.FiatQuoteType
import com.wallet.core.primitives.FiatQuoteUrlRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuyRepository @Inject constructor(
    private val configStore: com.gemwallet.android.data.service.store.ConfigStore,
    private val gemApi: GemApiClient,
    private val assetsRepository: AssetsRepository,
    private val getDeviceIdOld: GetDeviceIdOld,
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
        } catch (_: Throwable) { }
    }

    suspend fun getQuotes(
        asset: Asset,
        type: FiatQuoteType,
        fiatCurrency: String,
        amount: Double,
        owner: String,
    ): List<FiatQuote> {
        return try {
            when (type) {
                FiatQuoteType.Buy -> gemApi.getBuyFiatQuotes(
                    assetId = asset.id.toIdentifier(),
                    amount = amount,
                    currency = fiatCurrency,
                    deviceId = getDeviceIdOld.getDeviceId()
                )
                FiatQuoteType.Sell -> gemApi.getSellFiatQuotes(
                    assetId = asset.id.toIdentifier(),
                    amount = amount,
                    currency = fiatCurrency,
                    deviceId = getDeviceIdOld.getDeviceId()
                )
            }.quotes
        } catch (err: Throwable) {
            throw Exception("Quotes not found", err)
        }
    }

    suspend fun getQuoteUrl(quoteId: String, walletAddress: String, deviceId: String): String? {
        return try {
            gemApi.getFiatQuoteUrl(
                FiatQuoteUrlRequest(
                    quoteId = quoteId,
                    walletAddress = walletAddress,
                    deviceId = deviceId,
                )
            ).redirectUrl
        } catch (_: Throwable) {
            null
        }
    }

    private enum class ConfigKey(val string: String) {
        FiatAssetsVersion("fiat-assets-version"),
        FiatAssets("fiat-assets"),
    }
}