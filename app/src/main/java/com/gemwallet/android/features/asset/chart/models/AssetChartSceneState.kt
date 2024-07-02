package com.gemwallet.android.features.asset.chart.models

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetLinks
import com.wallet.core.primitives.ChartPeriod
import com.wallet.core.primitives.Currency

sealed interface AssetChartSceneState {
    data object Loading : AssetChartSceneState

    class Chart(
        val loading: Boolean,
        val assetId: AssetId,
        val assetTitle: String,
        val assetLinkTitle: String,
        val assetLink: String,
        val assetLinks: AssetLinks?,
        val currency: Currency,
        val marketCap: String,
        val circulatingSupply: String,
        val totalSupply: String,
        val period: ChartPeriod,
        val currentPoint: PricePoint?,
        val chartPoints: List<PricePoint>,
    ) : AssetChartSceneState
}