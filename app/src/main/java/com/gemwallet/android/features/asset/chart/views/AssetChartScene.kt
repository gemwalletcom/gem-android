package com.gemwallet.android.features.asset.chart.views

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.asset.chart.models.AssetMarketUIModel
import com.gemwallet.android.features.asset.chart.viewmodels.AssetChartViewModel
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.Badge
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.ListItem
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.open
import com.gemwallet.android.ui.components.screen.Scene
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetMarket
import com.wallet.core.primitives.Currency
import java.math.BigInteger

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
            assetMarket(marketModel.currency, marketModel.asset, marketModel.marketInfo)

            if (marketModel.assetLinks.isNotEmpty()) {
                item {
                    SubheaderItem(title = "LINKS")
                    Table(marketModel.assetLinks.map { it.toCell(uriHandler) })
                }
            }
        }
    }
}

private fun LazyListScope.assetMarket(currency: Currency, asset: Asset, marketInfo: AssetMarket?) {
    marketInfo ?: return
    marketInfo.marketCap?.let {
        item {
            ListItem(
                title = {
                    ListItemTitleText(
                        stringResource(R.string.asset_market_cap),
                        titleBadge = marketInfo.marketCapRank?.let { { Badge("#$it") } },
                    )
                },
                trailing = { ListItemTitleText(currency.format(it)) }
            )
        }
    }
    marketInfo.circulatingSupply?.let {
        item {
            ListItem(
                title = { ListItemTitleText(stringResource(R.string.asset_circulating_supply)) },
                trailing = { ListItemTitleText(Crypto(BigInteger.valueOf(it.toLong())).format(0, asset.symbol, 0)) }
            )
        }
    }
    marketInfo.totalSupply?.let {
        item {
            ListItem(
                title = { ListItemTitleText(stringResource(R.string.asset_total_supply)) },
                trailing = { ListItemTitleText(Crypto(BigInteger.valueOf(it.toLong())).format(0, asset.symbol, 0)) }
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