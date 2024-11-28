package com.gemwallet.android.ui.models

import androidx.compose.runtime.Stable
import com.gemwallet.android.ext.same
import com.gemwallet.android.ui.components.image.getIconUrl
import com.gemwallet.android.ui.components.image.getSupportIconUrl
import com.gemwallet.android.model.AssetInfo
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetMetaData
import com.wallet.core.primitives.Currency

interface AssetItemUIModel : CryptoFormattedUIModel, FiatFormattedUIModel {
    val name: String
    val symbol: String
    val assetIconUrl: String
        get() = asset.getIconUrl()
    val assetNetworkIconUrl: String?
        get() = asset.getSupportIconUrl()
    val price: PriceUIModel
    val owner: String
    val isZeroAmount: Boolean
    val position: Int
    val metadata: AssetMetaData?
}

@Stable
class AssetInfoUIModel(
    val assetInfo: AssetInfo,
) : AssetItemUIModel {

    override val asset: Asset
        get() =  assetInfo.asset

    override val name: String by lazy { asset.name }

    override val symbol: String by lazy { asset.symbol }

    override val cryptoAmount: Double
        get() = assetInfo.balance.totalAmount

    override val fiat: Double? by lazy {
        val price = assetInfo.price?.price?.price ?: 0.0
        if (price == 0.0) null else cryptoAmount * price
    }

    override val price: PriceUIModel by lazy {
        AssetPriceUIModel(currency, assetInfo.price?.price)
    }

    override val currency: Currency
        get() = assetInfo.price?.currency ?: Currency.USD

    override val owner: String by lazy { assetInfo.owner.address }

    override val isZeroAmount: Boolean by lazy { cryptoAmount == 0.0 }

    override val position: Int by lazy { assetInfo.position }

    override val metadata: AssetMetaData? by lazy { assetInfo.metadata }

    override fun equals(other: Any?): Boolean {
        return (other is AssetInfoUIModel) && other.asset.id.same(asset.id)
                && other.assetInfo.price == assetInfo.price
                && other.assetInfo.balance == assetInfo.balance
                && other.metadata?.isEnabled == assetInfo.metadata?.isEnabled
                && other.metadata?.isSwapEnabled == assetInfo.metadata?.isSwapEnabled
                && other.metadata?.isBuyEnabled == assetInfo.metadata?.isBuyEnabled
                && other.metadata?.isStakeEnabled == assetInfo.metadata?.isStakeEnabled
                && other.metadata?.isPinned == assetInfo.metadata?.isPinned
                && other.metadata?.isSellEnabled == assetInfo.metadata?.isSellEnabled
    }

    override fun hashCode(): Int {
        var result = assetInfo.hashCode()
        result = 31 * result + asset.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + symbol.hashCode()
        result = 31 * result + cryptoAmount.hashCode()
        result = 31 * result + (fiat?.hashCode() ?: 0)
        result = 31 * result + price.hashCode()
        result = 31 * result + currency.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + isZeroAmount.hashCode()
        result = 31 * result + position
        result = 31 * result + (metadata?.hashCode() ?: 0)
        return result
    }
}