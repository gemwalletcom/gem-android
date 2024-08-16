package com.gemwallet.android.features.asset.chart.models

import androidx.annotation.StringRes
import com.gemwallet.android.ui.components.CellEntity
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency

class AssetMarketUIModel(
    val assetId: AssetId,
    val assetTitle: String,
    val assetLinks: List<Link> = emptyList(),
    val currency: Currency = Currency.USD,
    val marketCells: List<CellEntity<Int>> = emptyList(),
) {
    class Link(
        val type: String,
        val url: String,
        @StringRes val label: Int,
    )
}