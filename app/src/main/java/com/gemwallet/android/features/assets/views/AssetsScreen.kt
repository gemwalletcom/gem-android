package com.gemwallet.android.features.assets.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.assets.model.WalletInfoUIState
import com.gemwallet.android.features.assets.viewmodel.AssetsViewModel
import com.gemwallet.android.ui.components.AmountListHead
import com.gemwallet.android.ui.components.AssetHeadActions
import com.gemwallet.android.ui.components.AssetListItem
import com.gemwallet.android.ui.components.DropDownContextItem
import com.gemwallet.android.ui.components.designsystem.Spacer4
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.models.AssetItemUIModel
import com.wallet.core.primitives.AssetId

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
                modifier = Modifier.testTag("assets_list"),
                state = listState
            ) {
                assetsHead(walletInfo, onSendClick, onReceiveClick, onBuyClick)
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

    items(items = assets, key = { it.asset.id.toIdentifier() }) { item ->
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

@Composable
private fun AssetItem(
    item: AssetItemUIModel,
    longPressState: MutableState<AssetId?>,
    modifier: Modifier = Modifier,
//    iconModifier: Modifier = Modifier,
    isPinned: Boolean = false,
    onAssetClick: (AssetId) -> Unit,
    onAssetHide: (AssetId) -> Unit,
    onTogglePin: (AssetId) -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    DropDownContextItem(
        modifier = modifier.testTag(item.asset.id.toIdentifier()),
        isExpanded = longPressState.value == item.asset.id,
        imeCompensate = false,
        onDismiss = { longPressState.value = null },
        content = { AssetListItem(item) },
        menuItems = {
            DropdownMenuItem(
                text = { Text( text = stringResource(id = if (isPinned) R.string.common_unpin else R.string.common_pin)) },
                trailingIcon = {
                    if (isPinned) Icon(painterResource(R.drawable.keep_off), "unpin")
                    else Icon(Icons.Default.PushPin, "pin")

                },
                onClick = {
                    onTogglePin(item.asset.id)
                    longPressState.value = null
                },
            )
            DropdownMenuItem(
                text = { Text( text = stringResource(id = R.string.wallet_copy_address)) },
                trailingIcon = { Icon(Icons.Default.ContentCopy, "copy") },
                onClick = {
                    clipboardManager.setText(AnnotatedString(item.owner))
                    longPressState.value = null
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.common_hide)) },
                trailingIcon = { Icon(Icons.Default.VisibilityOff, "wallet_config") },
                onClick = {
                    onAssetHide(item.asset.id)
                    longPressState.value = null
                }
            )
        },
        onLongClick = { longPressState.value = item.asset.id }
    ) { onAssetClick(item.asset.id) }
}

private fun LazyListScope.pinnedAssetsHeader() {
    item {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Default.PushPin,
                tint = MaterialTheme.colorScheme.secondary,
                contentDescription = "pinned_section",
            )
            Spacer4()
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.background),
                text = stringResource(R.string.common_pinned),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssetsTopBar(
    walletInfo: WalletInfoUIState,
    onShowWallets: () -> Unit,
    onShowAssetManage: () -> Unit,
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        title = {
            Box {
                TextButton(onClick = onShowWallets) {
                    Row(verticalAlignment = Alignment.CenterVertically ) {
                        AsyncImage(
                            model = walletInfo.icon,
                            placeholderText = null,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = walletInfo.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = "select_wallet",
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = onShowAssetManage, Modifier.testTag("assetsManageAction")) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "asset_manager",
                )
            }
        }
    )
}