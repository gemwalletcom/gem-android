package com.gemwallet.android.data.repositoreis.buy

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
        val fiatVersion = gemApi.getConfig().getOrNull()?.versions?.fiatOnRampAssets
        val currentVersion = configStore.getInt(ConfigKey.FiatAssetsVersion.string)
        if (currentVersion > 0 && currentVersion >= (fiatVersion ?: 0)) return
        val availableToBuyIds = gemApi.getOnRampAssets().getOrNull() ?: return
        val availableToSellIds = gemApi.getOffRampAssets().getOrNull()
        val latestVersion = availableToBuyIds.version.toInt()
        assetsRepository.updateBuyAvailable(availableToBuyIds.assetIds)
        assetsRepository.updateSellAvailable(availableToSellIds?.assetIds ?: emptyList())
        configStore.putInt(ConfigKey.FiatAssetsVersion.string, latestVersion)
    }

    suspend fun getQuotes(
        asset: Asset,
        type: FiatQuoteType,
        fiatCurrency: String,
        amount: Double,
        owner: String,
    ): List<FiatQuote> {
        return gemApi.getFiatQuotes(
            assetId = asset.id.toIdentifier(),
            type = type.string,
            fiatAmount = if (type == FiatQuoteType.Buy) amount else null,
            cryptoAmount = if (type == FiatQuoteType.Sell) Crypto(amount.toBigDecimal(), asset.decimals).atomicValue.toString() else null,
            currency = fiatCurrency,
            walletAddress = owner
        ).getOrNull()?.quotes ?: throw Exception("Quotes not found")
    }

    private enum class ConfigKey(val string: String) {
        FiatAssetsVersion("fiat-assets-version"),
        FiatAssets("fiat-assets"),
    }
}