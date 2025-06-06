package com.gemwallet.android.features.asset.chart.views

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.chain
import com.gemwallet.android.features.asset.chart.models.AssetMarketUIModel
import com.gemwallet.android.features.asset.chart.viewmodels.AssetChartViewModel
import com.gemwallet.android.model.compactFormatter
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.Badge
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.list_item.PropertyDataText
import com.gemwallet.android.ui.components.list_item.PropertyItem
import com.gemwallet.android.ui.components.list_item.PropertyTitleText
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.open
import com.gemwallet.android.ui.components.screen.Scene
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetMarket
import com.wallet.core.primitives.Currency
import uniffi.gemstone.Explorer

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

            if (marketModel.assetLinks.isNotEmpty()) {
                item {
                    SubheaderItem(title = "LINKS")
                    Table(marketModel.assetLinks.map { it.toCell(uriHandler) })
                }
            }
        }
    }
}

private fun LazyListScope.assetMarket(currency: Currency, asset: Asset, marketInfo: AssetMarket?, explorerName: String) {
    marketInfo?.marketCap?.let {
        item {
            PropertyItem(
                title = {
                    PropertyTitleText(
                        text = R.string.asset_market_cap,
                        badge = marketInfo.marketCapRank?.takeIf { it > 0 }
                            .let { { Badge("#${marketInfo.marketCapRank}") } }
                    )
                },
                data = { PropertyDataText(currency.compactFormatter(it)) }
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
                        uriHandler.open(Explorer(asset.chain().string).getTokenUrl(explorerName, it) ?: return@combinedClickable)
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

@Composable
private fun AssetMarketUIModel.Link.toCell(uriHandler: UriHandler): CellEntity<Int> {
    return CellEntity(
        label = label,
        icon = icon,
        data = "",
        action = { uriHandler.open(url) }
    )
}