package com.gemwallet.android.features.asset.chart.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetMarket
import com.wallet.core.primitives.Currency

class AssetMarketUIModel(
    val asset: Asset,
    val assetTitle: String,
    val assetLinks: List<Link> = emptyList(),
    val currency: Currency = Currency.USD,
    val explorerName: String,
    val marketInfo: AssetMarket? = null,
) {
    class Link(
        val type: String,
        val url: String,
        @StringRes val label: Int,
        @DrawableRes val icon: Int,
    )
}