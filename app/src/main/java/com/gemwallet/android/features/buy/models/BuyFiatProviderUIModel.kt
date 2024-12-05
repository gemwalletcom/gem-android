package com.gemwallet.android.features.buy.models

import androidx.compose.runtime.Stable
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.models.CryptoFormattedUIModel
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.FiatProvider
import com.wallet.core.primitives.FiatQuote

@Stable
data class BuyFiatProviderUIModel(
    val provider: FiatProvider,
    override val asset: Asset,
    override val cryptoAmount: Double,
    val rate: String,
    val redirectUrl: String?,
) : CryptoFormattedUIModel {
    override val cryptoFormatted: String
        get() = "≈ ${super.cryptoFormatted}"
}

fun FiatQuote.toProviderUIModel(
    asset: Asset,
    currency: Currency,
) = BuyFiatProviderUIModel(
    provider = provider,
    asset = asset,
    cryptoAmount = cryptoAmount,
    rate = asset.rateText(fiatAmount, cryptoAmount, currency),
    redirectUrl = redirectUrl
)

private fun Asset.rateText(fiatAmount: Double, cryptoAmount: Double, currency: Currency) =
    "1 $symbol ≈ ${currency.format(fiatAmount / cryptoAmount).format(currency.string, 2)}"


