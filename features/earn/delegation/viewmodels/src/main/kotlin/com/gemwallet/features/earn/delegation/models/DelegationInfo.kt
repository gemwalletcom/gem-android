package com.gemwallet.features.earn.delegation.models

import androidx.compose.runtime.Stable
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.ui.models.CryptoFormattedUIModel
import com.gemwallet.android.ui.models.FiatFormattedUIModel
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Delegation

interface DelegationInfoUIModel {

    val iconUrl: String
}

@Stable
class HeadDelegationInfo(
    private val delegation: Delegation,
    private val assetInfo: AssetInfo,
    override val maxFraction: Int = -1,
    override val fraction: Int = -1,
) : DelegationInfoUIModel, CryptoFormattedUIModel, FiatFormattedUIModel {

    override val iconUrl: String
        get() = "https://assets.gemwallet.com/blockchains/${delegation.validator.chain.string}/validators/${delegation.validator.id}/logo.png"

    override val cryptoAmount: Double by lazy {
        Crypto(delegation.base.balance).value(asset.decimals).toDouble()
    }

    override val currency: Currency
        get() = assetInfo.price?.currency ?: Currency.USD

    override val fiat: Double? by lazy {
        val price = assetInfo.price?.price?.price ?: 0.0
        if (price == 0.0) null else cryptoAmount * price
    }

    override val asset: Asset
        get() = assetInfo.asset

}