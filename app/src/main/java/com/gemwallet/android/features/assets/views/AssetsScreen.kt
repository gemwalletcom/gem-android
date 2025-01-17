package com.gemwallet.android.features.assets.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.assets.model.WalletInfoUIState
import com.gemwallet.android.features.assets.viewmodel.AssetsViewModel
import com.gemwallet.android.features.banners.views.BannersScene
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.AmountListHead
import com.gemwallet.android.ui.components.AssetHeadActions
import com.gemwallet.android.ui.components.open
import com.gemwallet.android.ui.components.pinnedAssetsHeader
import com.gemwallet.android.ui.models.AssetItemUIModel
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.BannerEvent
import uniffi.gemstone.Config
import uniffi.gemstone.DocsUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    onShowWallets: () -> Unit,
    onShowAssetManage: () -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onBuyClick: () -> Unit,
    onAssetClick: (AssetId) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    viewModel: AssetsViewModel = hiltViewModel(),
) {
    val pinnedAssets by viewModel.pinnedAssets.collectAsStateWithLifecycle()
    val unpinnedAssets by viewModel.unpinnedAssets.collectAsStateWithLifecycle()
    val walletInfo by viewModel.walletInfo.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AssetsTopBar(walletInfo, onShowWallets, onShowAssetManage) }
    ) {
        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            modifier = Modifier.padding(top = it.calculateTopPadding()),
            isRefreshing = screenState,
            onRefresh = viewModel::onRefresh,
            state = pullToRefreshState,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = screenState,
                    state = pullToRefreshState,
                    containerColor = MaterialTheme.colorScheme.background
                )
            }
        ) {
            val longPressedAsset = remember { mutableStateOf<AssetId?>(null) }
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .testTag("assets_list"),
                state = listState
            ) {
                assetsHead(walletInfo, onSendClick, onReceiveClick, onBuyClick)
                item {
                    BannersScene(
                        asset = null,
                        onClick = {
                            when (it.event) {
                                BannerEvent.AccountBlockedMultiSignature ->
                                    uriHandler.open(Config().getDocsUrl(DocsUrl.TRON_MULTI_SIGNATURE))
                                else -> {}
                            }
                        },
                        false
                    )
                    HorizontalDivider(thickness = 0.dp)
                }
                assets(
                    assets = pinnedAssets,
                    longPressState = longPressedAsset,
                    isPinned = true,
                    onAssetClick = onAssetClick,
                    onAssetHide = viewModel::hideAsset,
                    onTogglePin = viewModel::togglePin,
                )
                assets(
                    assets = unpinnedAssets,
                    longPressState = longPressedAsset,
                    isPinned = false,
                    onAssetClick = onAssetClick,
                    onAssetHide = viewModel::hideAsset,
                    onTogglePin = viewModel::togglePin,
                )
                assetsListFooter(onShowAssetManage)
            }
        }
    }
}

private fun LazyListScope.assetsHead(
    walletInfo: WalletInfoUIState,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onBuyClick: () -> Unit,
) {
    item {
        AmountListHead(
            amount = walletInfo.totalValue,
            actions = {
                AssetHeadActions(
                    walletType = walletInfo.type,
                    onTransfer = onSendClick,
                    transferEnabled = true,
                    onReceive = onReceiveClick,
                    onBuy = onBuyClick,
                    onSwap = null, // if (swapEnabled) onSwapClick else null
                )
            }
        )
    }
}

private fun LazyListScope.assetsListFooter(
    onShowAssetManage: () -> Unit,
) {
    item {
        Box(
            modifier = Modifier
                .clickable(onClick = onShowAssetManage)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "asset_manager",
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSize))
                Text(
                    text = stringResource(id = R.string.wallet_manage_token_list),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.assets(
    assets: List<AssetItemUIModel>,
    longPressState: MutableState<AssetId?>,
    isPinned: Boolean = false,
    onAssetClick: (AssetId) -> Unit,
    onAssetHide: (AssetId) -> Unit,
    onTogglePin: (AssetId) -> Unit,
) {
    if (assets.isEmpty()) return

    if (isPinned) {
        pinnedAssetsHeader()
    }

    items(items = assets, key = { "${it.asset.id.toIdentifier()}-$isPinned" }) { item ->
        AssetItem(
            modifier = Modifier.testTag(item.asset.id.toIdentifier()),
            item = item,
            longPressState = longPressState,
            isPinned = isPinned,
            onAssetClick = onAssetClick,
            onAssetHide = onAssetHide,
            onTogglePin = onTogglePin,
        )
    }
    if (isPinned) {
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}