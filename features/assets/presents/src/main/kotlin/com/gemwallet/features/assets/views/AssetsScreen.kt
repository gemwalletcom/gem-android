@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.gemwallet.features.assets.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.update_app.presents.InAppUpdateBanner
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.models.AssetsGroupType
import com.gemwallet.android.ui.open
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingSmall
import com.gemwallet.features.assets.viewmodels.AssetsViewModel
import com.gemwallet.features.assets.views.components.AssetsHead
import com.gemwallet.features.assets.views.components.AssetsListFooter
import com.gemwallet.features.assets.views.components.assets
import com.gemwallet.features.banner.views.BannersScene
import com.gemwallet.features.banner.views.WelcomeBanner
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.BannerEvent
import uniffi.gemstone.Config
import uniffi.gemstone.DocsUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    onShowWallets: () -> Unit,
    onManage: () -> Unit,
    onSearch: () -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onBuyClick: () -> Unit,
    onAssetClick: (AssetId) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    viewModel: AssetsViewModel = hiltViewModel(),
) {
    val importing by viewModel.importInProgress.collectAsStateWithLifecycle()
    val pinnedAssets by viewModel.pinnedAssets.collectAsStateWithLifecycle()
    val unpinnedAssets by viewModel.unpinnedAssets.collectAsStateWithLifecycle()
    val walletInfo by viewModel.walletInfo.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val isWelcomeBannerHidden by viewModel.isWelcomeBannerHidden.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { AssetsTopBar(walletInfo, onShowWallets, onSearch) },
        containerColor = MaterialTheme.colorScheme.surface,
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
                item {
                    AssetsHead(
                        walletInfo = walletInfo,
                        onSendClick = onSendClick,
                        onReceiveClick = onReceiveClick,
                        onBuyClick = onBuyClick,
                        onHideBalances = viewModel::hideBalances
                    )
                }
                item {
                    WelcomeBanner(
                        isHidden = isWelcomeBannerHidden,
                        isSynced = importing,
                        cryptoTotal = walletInfo.cryptoTotalValue,
                        onBuy = onBuyClick,
                        onReceive = onReceiveClick,
                        onClose = viewModel::onHideWelcomeBanner
                    )
                }
                item {
                    InAppUpdateBanner()
                    BannersScene(
                        asset = null,
                        onClick = { banner ->
                            when (banner.event) {
                                BannerEvent.AccountBlockedMultiSignature ->
                                    uriHandler.open(context, Config().getDocsUrl(DocsUrl.TronMultiSignature))
                                else -> {}
                            }
                        },
                        false
                    )
                }
                if (importing) {
                    item {
                        Row(
                            modifier = Modifier.padding(paddingDefault),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(paddingSmall)
                        ) {
                            Text(
                                text = "${stringResource(R.string.common_loading)}…"
                            )
                            CircularWavyProgressIndicator(
                                modifier = Modifier.size(paddingDefault),
                                stroke = Stroke(
                                    width = with(LocalDensity.current) { 2.dp.toPx() },
                                    cap = StrokeCap.Round,
                                ),
                                trackStroke = Stroke(
                                    width = with(LocalDensity.current) { 2.dp.toPx() },
                                    cap = StrokeCap.Round,
                                )
                            )
                        }
                    }
                }
                assets(
                    items = pinnedAssets,
                    longPressState = longPressedAsset,
                    group = AssetsGroupType.Pined,
                    onAssetClick = onAssetClick,
                    onAssetHide = viewModel::hideAsset,
                    onTogglePin = viewModel::togglePin,
                )
                assets(
                    items = unpinnedAssets,
                    longPressState = longPressedAsset,
                    group = AssetsGroupType.None,
                    onAssetClick = onAssetClick,
                    onAssetHide = viewModel::hideAsset,
                    onTogglePin = viewModel::togglePin,
                )
                item { AssetsListFooter(onManage) }
            }
        }
    }
}