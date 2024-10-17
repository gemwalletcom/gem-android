package com.gemwallet.android.ui.models

import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetMetaData
import com.wallet.core.primitives.Currency
import java.math.BigInteger

interface AssetItemUIModel : CryptoFormatterUIModel,  FiatFormatterUIModel {
    val name: String
    val symbol: String
}

class AssetInfoUIModel(
    val assetInfo: AssetInfo,
) : AssetItemUIModel {

//    asset = asset,
//        isZeroValue = balances.atomicValue == BigInteger.ZERO,
//        value = asset.format(balances, 4),
//        price = PriceUIState.create(price?.price, currency),
//        fiat = if (price?.price == null || price!!.price.price == 0.0) {
//            ""
//        } else {
//            currency.format(balances.convert(asset.decimals, price!!.price.price), 2)
//        },
//        owner = owner.address,
//        metadata = metadata,
//        position = position,

    override val priceValue: Double? by lazy { assetInfo.price?.price?.price }

    override val asset: Asset by lazy { assetInfo.asset }

    override val name: String by lazy { assetInfo.asset.name }

    override val symbol: String by lazy { assetInfo.asset.symbol }

    override val crypto: Crypto by lazy { assetInfo.balances.available() }

    override val currency: Currency by lazy { assetInfo.price?.currency ?: Currency.USD }

    val owner: String by lazy { assetInfo.owner.address }

    val isZeroAmount: Boolean by lazy { assetInfo.balances.calcTotal().atomicValue == BigInteger.ZERO }

    val position: Int by lazy { assetInfo.position }

    val metadata: AssetMetaData? by lazy { assetInfo.metadata }
}