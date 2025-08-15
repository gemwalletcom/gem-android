package com.gemwallet.features.asset.presents.chart.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.chain
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
import com.gemwallet.android.ui.open
import com.gemwallet.android.ui.theme.trailingIconMedium
import com.gemwallet.features.asset.viewmodels.chart.models.AssetMarketUIModel
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
//            item {
//                LineChart(
//                    modifier = Modifier.fillMaxWidth().height(200.dp),
//                    labelHelperProperties = LabelHelperProperties(enabled = false),
//                    gridProperties = GridProperties(enabled = false),
//                    indicatorProperties = HorizontalIndicatorProperties(enabled = false),
//                    zeroLineProperties = ZeroLineProperties(enabled = false),
//                    dividerProperties = DividerProperties(enabled = false),
//                    data = remember {
//                        listOf(
//                            Line(
//                                label = "price",
//                                values = listOf(28.0, 41.0, 5.0, 10.0, 35.0),
//                                color = SolidColor(Color(0xFF23af92)),
////                                firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
////                                secondGradientFillColor = Color.Transparent,
//                                strokeAnimationSpec = tween(500, easing = EaseInOutCubic),
//                                drawStyle = DrawStyle.Stroke(width = 2.dp),
//                            )
//                        )
//                    },
//                    minValue = 5.0,
//                    maxValue = 41.0,
//                    animationMode = AnimationMode.Together(delayBuilder = { it * 500L }),
//                )
//            }
            item { Chart() }
            assetMarket(marketModel.currency, marketModel.asset, marketModel.marketInfo, marketModel.explorerName)
            links(marketModel.assetLinks)
        }
    }
}

private fun LazyListScope.links(links: List<AssetMarketUIModel.Link>) {
    if (links.isEmpty()) return
    item { SubheaderItem(title = "LINKS") }
    items(links) {
        val uriHandler = LocalUriHandler.current
        val context = LocalContext.current
        PropertyItem(
            modifier = Modifier.clickable { uriHandler.open(context, it.url) },
            title = { PropertyTitleText(it.label, trailing = { AsyncImage(it.icon, trailingIconMedium) }) },
            data = { PropertyDataText("", badge = { DataBadgeChevron() }) }
        )
    }
}

private fun LazyListScope.assetMarket(currency: Currency, asset: Asset, marketInfo: AssetMarket?, explorerName: String) {
    marketInfo?.marketCap?.let { cap ->
        item {
            PropertyItem(
                title = {
                    PropertyTitleText(
                        text = R.string.asset_market_cap,
                        badge = marketInfo.marketCapRank?.takeIf { it > 0 }
                            .let { { Badge("#${marketInfo.marketCapRank}") } }
                    )
                },
                data = { PropertyDataText(currency.compactFormatter(cap)) }
            )
        }
    }
    marketInfo?.circulatingSupply?.let {
        item { PropertyItem(R.string.asset_circulating_supply, asset.compactFormatter(it)) }
    }
    marketInfo?.totalSupply?.let {
        item { PropertyItem(R.string.asset_total_supply, asset.compactFormatter(it)) }
    }
    asset.id.tokenId?.let {
        item {
            val context = LocalContext.current
            val clipboardManager = LocalClipboard.current.nativeClipboard
            val uriHandler = LocalUriHandler.current
            PropertyItem(
                modifier = Modifier.combinedClickable(
                    onLongClick = {
                        clipboardManager.setPlainText(context, it)
                    },
                    onClick = {
                        uriHandler.open(context, Explorer(asset.chain().string).getTokenUrl(explorerName, it) ?: return@combinedClickable)
                    }
                ),
                title = { PropertyTitleText(R.string.asset_contract) },
                data = {
                    PropertyDataText(
                        modifier = Modifier.weight(1f).padding(top = 0.dp, bottom = 2.dp),
                        text = it,
                    )
                }
            )
        }
    }
}