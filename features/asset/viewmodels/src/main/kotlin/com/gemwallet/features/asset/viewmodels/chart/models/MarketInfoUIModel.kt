package com.gemwallet.features.asset.viewmodels.chart.models

import androidx.annotation.StringRes
import com.gemwallet.android.ui.R

class MarketInfoUIModel(
    val type: MarketInfoTypeUIModel,
    val value: String,
    val badge: String? = null,
) {
    enum class MarketInfoTypeUIModel(@param:StringRes val label: Int) {
        MarketCap(R.string.asset_market_cap),
        CirculatingSupply(R.string.asset_circulating_supply),
        TotalSupply(R.string.asset_total_supply),
        Contract(R.string.asset_contract)
    }
}