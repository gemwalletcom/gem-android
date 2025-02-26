package com.gemwallet.android.features.asset.chart.views

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.asset.chart.models.AssetMarketUIModel
import com.gemwallet.android.features.asset.chart.viewmodels.AssetChartViewModel
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.open
import com.gemwallet.android.ui.components.screen.Scene
import com.wallet.core.primitives.AssetId

@Composable
fun AssetChartScene(
    assetId: AssetId,
    viewModel: AssetChartViewModel = hiltViewModel(),
    onCancel: () -> Unit,
) {
    val marketUIModelState by viewModel.marketUIModel.collectAsStateWithLifecycle()

    val marketModel = marketUIModelState
    if (marketModel == null) {
        LoadingScene(assetId.chain.string, onCancel)
        return
    }

    val uriHandler = LocalUriHandler.current
    Scene(
        title = marketModel.assetTitle,
        backHandle = true,
        onClose = onCancel,
    ) {
        LazyColumn {
            item { Chart() }
            item { Table(items = marketModel.marketCells,) }

            if (marketModel.assetLinks.isNotEmpty()) {
                item {
                    SubheaderItem(title = "LINKS")
                    Table(marketModel.assetLinks.map { it.toCell(uriHandler) })
                }
            }
        }
    }
}

@Composable
private fun AssetMarketUIModel.Link.toCell(uriHandler: UriHandler): CellEntity<Int> {
    return CellEntity(
        label = label,
        icon = icon,
        data = "",
        action = { uriHandler.open(url) }
    )
}