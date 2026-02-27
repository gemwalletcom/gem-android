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
        MaxSupply(R.string.info_max_supply_title),
        Contract(R.string.asset_contract),
        FDV(R.string.info_fully_diluted_valuation_title),
    }
}

sealed class AllTimeUIModel(
    val date: Long,
    val value: Double,
    val percentage: Double,
) {
    class High(
        date: Long,
        value: Double,
        percentage: Double,
    ) : AllTimeUIModel(date, value, percentage)

    class Low(
        date: Long,
        value: Double,
        percentage: Double,
    ) : AllTimeUIModel(date, value, percentage)
}