package com.gemwallet.android.ui.models

import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.interactors.getSupportIconUrl
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fiat
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetMetaData
import com.wallet.core.primitives.Currency
import java.math.BigInteger

interface AssetItemUIModel : CryptoFormattedUIModel,  FiatFormattedUIModel {
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

class AssetInfoUIModel(
    val assetInfo: AssetInfo,
) : AssetItemUIModel {

    override val asset: Asset
        get() =  assetInfo.asset

    override val name: String by lazy { asset.name }

    override val symbol: String by lazy { asset.symbol }

    override val crypto: Crypto
        get() = assetInfo.balances.calcTotal()

    override val fiat: Fiat? by lazy {
        val price = assetInfo.price?.price?.price ?: 0.0
        if (price == 0.0) {
            null
        } else {
            crypto.convert(asset.decimals, price)
        }
    }

    override val price: PriceUIModel by lazy {
        AssetPriceUIModel(currency, assetInfo.price?.price)
    }

    override val currency: Currency
        get() = assetInfo.price?.currency ?: Currency.USD

    override val owner: String by lazy { assetInfo.owner.address }

    override val isZeroAmount: Boolean by lazy { crypto.atomicValue == BigInteger.ZERO }

    override val position: Int by lazy { assetInfo.position }

    override val metadata: AssetMetaData? by lazy { assetInfo.metadata }
}