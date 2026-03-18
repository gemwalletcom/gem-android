package com.gemwallet.features.asset.presents.chart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.domains.percentage.formatAsPercentage
import com.gemwallet.android.domains.price.getPriceState
import com.gemwallet.android.model.compactFormatter
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.list_item.Badge
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.color
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.open
import com.gemwallet.android.ui.theme.trailingIconMedium
import com.gemwallet.features.asset.viewmodels.chart.models.AllTimeUIModel
import com.gemwallet.features.asset.viewmodels.chart.models.AssetMarketUIModel
import com.gemwallet.features.asset.viewmodels.chart.models.MarketInfoUIModel
import com.gemwallet.features.asset.viewmodels.chart.viewmodels.AssetChartViewModel
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetMarket
import com.wallet.core.primitives.Currency
import uniffi.gemstone.Explorer
import java.text.DateFormat
import java.util.Date

@Composable
fun AssetChartScene(
    onCancel: () -> Unit,
    onPriceAlerts: (AssetId) -> Unit,
    onAddPriceAlertTarget: (AssetId) -> Unit,
    viewModel: AssetChartViewModel = hiltViewModel(),
) {
    val marketUIModelState by viewModel.marketUIModel.collectAsStateWithLifecycle()
    val priceAlertsCount by viewModel.priceAlertsCount.collectAsStateWithLifecycle()

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
            item {
                if (priceAlertsCount > 0) {
                    PropertyItem(
                        modifier = Modifier
                            .clickable { onPriceAlerts(marketModel.asset.id) }
                            .testTag("assetChart"),
                        title = { PropertyTitleText(R.string.settings_price_alerts_title) },
                        data = { PropertyDataText(text = "$priceAlertsCount", badge = { DataBadgeChevron() }) },
                        listPosition = ListPosition.Single,
                    )
                } else {
                    PropertyItem(
                        modifier = Modifier
                            .clickable { onAddPriceAlertTarget(marketModel.asset.id) }
                            .testTag("assetChart"),
                        title = { PropertyTitleText(R.string.price_alerts_set_alert_title) },
                        data = { PropertyDataText(text = "", badge = { DataBadgeChevron() }) },
                        listPosition = ListPosition.Single,
                    )
                }

            }
            assetMarket(marketModel.currency, marketModel.asset, marketModel.marketInfo, marketModel.explorerName)
            links(marketModel.assetLinks)
        }
    }
}

private fun LazyListScope.links(links: List<AssetMarketUIModel.Link>) {
    if (links.isEmpty()) return
    item { SubheaderItem(R.string.social_links) }
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
    marketInfo ?: return
    val marketItems = mutableListOf<MarketInfoUIModel>().apply {
        marketInfo.marketCap?.let {
            add(
                MarketInfoUIModel(
                    type = MarketInfoUIModel.MarketInfoTypeUIModel.MarketCap,
                    value = currency.compactFormatter(it),
                    badge = marketInfo.marketCapRank?.takeIf { it > 0 }
                        .let { "#${marketInfo.marketCapRank}" }
                )
            )
        }
        marketInfo.marketCapFdv?.let {
            add(
                MarketInfoUIModel(
                    type = MarketInfoUIModel.MarketInfoTypeUIModel.FDV,
                    value = currency.compactFormatter(it),
                    info = InfoSheetEntity.FullyDilutedValuation,
                )
            )
        }
    }

    val supplyItems = mutableListOf<MarketInfoUIModel>().apply {
        marketInfo.circulatingSupply?.let {
            add(
                MarketInfoUIModel(
                    type = MarketInfoUIModel.MarketInfoTypeUIModel.CirculatingSupply,
                    value = currency.compactFormatter(it),
                    info = InfoSheetEntity.CirculatingSupply,
                )
            )
        }
        marketInfo.totalSupply?.let {
            add(
                MarketInfoUIModel(
                    type = MarketInfoUIModel.MarketInfoTypeUIModel.TotalSupply,
                    value = currency.compactFormatter(it),
                    info = InfoSheetEntity.TotalSupply,
                )
            )
        }

        marketInfo.maxSupply?.let {
            add(
                MarketInfoUIModel(
                    type = MarketInfoUIModel.MarketInfoTypeUIModel.MaxSupply,
                    value = currency.compactFormatter(it),
                    info = InfoSheetEntity.MaxSupply,
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

    val allTime = mutableListOf<AllTimeUIModel>().apply {
        marketInfo.allTimeHighValue?.let {
            add(
                AllTimeUIModel.High(it.date, it.value.toDouble(), it.percentage.toDouble())
            )
        }

        marketInfo.allTimeLowValue?.let {
            add(
                AllTimeUIModel.Low(it.date, it.value.toDouble(), it.percentage.toDouble())
            )
        }
    }

    marketProperties(asset, explorerName, marketItems)
    marketProperties(asset, explorerName, supplyItems)
    allTimeProperties(asset, currency, allTime)
}

private fun LazyListScope.marketProperties(asset: Asset, explorerName: String, items: List<MarketInfoUIModel>) {
    itemsPositioned(items) { position, item ->
        when (item.type) {
            MarketInfoUIModel.MarketInfoTypeUIModel.FDV,
            MarketInfoUIModel.MarketInfoTypeUIModel.CirculatingSupply,
            MarketInfoUIModel.MarketInfoTypeUIModel.TotalSupply,
            MarketInfoUIModel.MarketInfoTypeUIModel.MaxSupply -> PropertyItem(item.type.label, item.value, listPosition = position, info = item.info)
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
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 0.dp, bottom = 2.dp),
                            text = item.value,
                        )
                    },
                    listPosition = position
                )
            }
        }
    }
}

private fun LazyListScope.allTimeProperties(asset: Asset, currency: Currency, items: List<AllTimeUIModel>) {
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

    itemsPositioned(items) { position, item ->
        val title = when (item) {
            is AllTimeUIModel.High -> R.string.asset_all_time_high
            is AllTimeUIModel.Low -> R.string.asset_all_time_low
        }
        ListItem(
            listPosition = position,
            title = @Composable { ListItemTitleText(stringResource(title)) },
            subtitle = { ListItemSupportText(dateFormat.format(Date(item.date))) },
            trailing = {
                Column(
                    modifier = Modifier.defaultMinSize(40.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    ListItemTitleText(currency.compactFormatter(item.value))
                    ListItemSupportText(item.percentage.formatAsPercentage(), color = item.percentage.getPriceState().color())
                }
           },
        )
    }

}