package com.gemwallet.android.ui.models

import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationBase
import java.math.BigInteger

open class BalanceInfoUIModel(
    override val asset: Asset,
    balance: BigInteger,
    val price: Double?,
    override val currency: Currency
) : AssetUIModel, CryptoFormattedUIModel, FiatFormattedUIModel {

    override val cryptoAmount: Double by lazy { Crypto(balance).value(asset.decimals).toDouble() }

    override val fiat: Double? by lazy {
        val price = price ?: 0.0
        if (price == 0.0) null else cryptoAmount * price
    }
}

class RewardsInfoUIModel(
    assetInfo: AssetInfo,
    balance: String,
) : BalanceInfoUIModel(
    asset = assetInfo.asset,
    balance = BigInteger(balance),
    price = assetInfo.price?.price?.price,
    currency = assetInfo.price?.currency ?: Currency.USD,
)

class DelegationBalanceInfoUIModel(
    assetInfo: AssetInfo,
    delegation: DelegationBase,
) : BalanceInfoUIModel(
    asset = assetInfo.asset,
    balance = BigInteger(delegation.balance),
    price = assetInfo.price?.price?.price,
    currency = assetInfo.price?.currency ?: Currency.USD,
)