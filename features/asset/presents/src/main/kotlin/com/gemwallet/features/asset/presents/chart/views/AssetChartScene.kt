package com.gemwallet.features.asset.presents.chart.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.model.compactFormatter
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.list_item.Badge
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.open
import com.gemwallet.android.ui.theme.trailingIconMedium
import com.gemwallet.features.asset.viewmodels.chart.models.AssetMarketUIModel
import com.gemwallet.features.asset.viewmodels.chart.models.MarketInfoUIModel
import com.gemwallet.features.asset.viewmodels.chart.viewmodels.AssetChartViewModel
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetMarket
import com.wallet.core.primitives.Currency
import uniffi.gemstone.Explorer

@Composable
fun AssetChartScene(
    viewModel: AssetChartViewModel = hiltViewModel(),
    onCancel: () -> Unit,
) {
    val marketUIModelState by viewModel.marketUIModel.collectAsStateWithLifecycle()

    val marketModel = marketUIModelState
    if (marketModel == null) {
        LoadingScene(stringResource(R.string.common_loading), onCancel)
        return
    }

    Scene(
        title = marketModel.assetTitle,
        backHandle = true,
        onClose = onCancel,
    ) {
        LazyColumn {
            item { Chart() }
            assetMarket(marketModel.currency, marketModel.asset, marketModel.marketInfo, marketModel.explorerName)
            links(marketModel.assetLinks)
        }
    }
}

private fun LazyListScope.links(links: List<AssetMarketUIModel.Link>) {
    if (links.isEmpty()) return
    item { SubheaderItem(title = "LINKS") }
    itemsIndexed(links) { index, item ->
        val uriHandler = LocalUriHandler.current
        val context = LocalContext.current
        PropertyItem(
            modifier = Modifier.clickable { uriHandler.open(context, item.url) },
            title = { PropertyTitleText(item.label, trailing = { AsyncImage(item.icon, trailingIconMedium) }) },
            data = { PropertyDataText("", badge = { DataBadgeChevron() }) },
            listPosition = ListPosition.getPosition(index, links.size)
        )
    }
}

private fun LazyListScope.assetMarket(currency: Currency, asset: Asset, marketInfo: AssetMarket?, explorerName: String) {
    val items = marketInfo?.let {
        mutableListOf<MarketInfoUIModel>().apply {
            it.marketCap?.let {
                add(
                    MarketInfoUIModel(
                        type = MarketInfoUIModel.MarketInfoTypeUIModel.MarketCap,
                        value = currency.compactFormatter(it),
                        badge = marketInfo.marketCapRank?.takeIf { it > 0 }
                            .let { "#${marketInfo.marketCapRank}" }
                    )
                )
            }
            it.circulatingSupply?.let {
                add(
                    MarketInfoUIModel(
                        type = MarketInfoUIModel.MarketInfoTypeUIModel.CirculatingSupply,
                        value = currency.compactFormatter(it),
                    )
                )
            }
            it.totalSupply?.let {
                add(
                    MarketInfoUIModel(
                        type = MarketInfoUIModel.MarketInfoTypeUIModel.TotalSupply,
                        value = currency.compactFormatter(it),
                    )
                )
            }
            asset.id.tokenId?.let {
                add(
                    MarketInfoUIModel(
                        type = MarketInfoUIModel.MarketInfoTypeUIModel.Contract,
                        value = it,
                    )
                )
            }
        }
    } ?: emptyList()
    itemsIndexed(items) { index, item ->
        val position = ListPosition.getPosition(index, items.size)
        when (item.type) {
            MarketInfoUIModel.MarketInfoTypeUIModel.MarketCap -> PropertyItem(
                title = {
                    PropertyTitleText(
                        text = item.type.label,
                        badge = item.badge?.let { { Badge(it) } }
                    )
                },
                data = { PropertyDataText(item.value) },
                listPosition = position
            )
            MarketInfoUIModel.MarketInfoTypeUIModel.CirculatingSupply -> PropertyItem(item.type.label, item.value, listPosition = position)
            MarketInfoUIModel.MarketInfoTypeUIModel.TotalSupply -> PropertyItem(item.type.label, item.value, listPosition = position)
            MarketInfoUIModel.MarketInfoTypeUIModel.Contract -> {
                val context = LocalContext.current
                val clipboardManager = LocalClipboard.current.nativeClipboard
                val uriHandler = LocalUriHandler.current
                PropertyItem(
                    modifier = Modifier.combinedClickable(
                        onLongClick = {
                            clipboardManager.setPlainText(context, item.value)
                        },
                        onClick = {
                            uriHandler.open(context, Explorer(asset.chain.string).getTokenUrl(explorerName, item.value) ?: return@combinedClickable)
                        }
                    ),
                    title = { PropertyTitleText(R.string.asset_contract) },
                    data = {
                        PropertyDataText(
                            modifier = Modifier.weight(1f).padding(top = 0.dp, bottom = 2.dp),
                            text = item.value,
                        )
                    },
                    listPosition = position
                )
            }
        }
    }
}