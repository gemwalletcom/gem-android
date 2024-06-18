package com.gemwallet.android.features.assets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.assets.model.AssetUIState
import com.gemwallet.android.features.assets.model.PriceState
import com.gemwallet.android.features.assets.model.PriceUIState
import com.gemwallet.android.features.assets.model.WalletInfoUIState
import com.gemwallet.android.features.transactions.components.transactionsList
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.ui.components.AmountListHead
import com.gemwallet.android.ui.components.AssetHeadActions
import com.gemwallet.android.ui.components.AssetListItem
import com.gemwallet.android.ui.components.AsyncImage
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.WalletTheme
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionExtended
import com.wallet.core.primitives.WalletType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun AssetsScreen(
    onShowWallets: () -> Unit,
    onShowAssetManage: () -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onBuyClick: () -> Unit,
    onSwapClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onAssetClick: (AssetId) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    viewModel: AssetsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    UI(
        isLoading = uiState.isLoading,
        walletInfo = uiState.walletInfo,
        assets = uiState.assets,
        transactions = uiState.pendingTransactions,
        swapEnabled = uiState.swapEnabled,
        onRefresh = viewModel::onRefresh,
        onShowWallets = onShowWallets,
        onShowAssetManage = onShowAssetManage,
        onSendClick = onSendClick,
        onReceiveClick = onReceiveClick,
        onBuyClick = onBuyClick,
        onSwapClick = onSwapClick,
        onTransactionClick = onTransactionClick,
        onAssetClick = onAssetClick,
        onAssetHide = viewModel::hideAsset,
        listState = listState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UI(
    isLoading: Boolean,
    walletInfo: WalletInfoUIState,
    assets: ImmutableList<AssetUIState>,
    transactions: ImmutableList<TransactionExtended>,
    swapEnabled: Boolean,
    onRefresh: () -> Unit,
    onShowWallets: () -> Unit,
    onShowAssetManage: () -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onBuyClick: () -> Unit,
    onSwapClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onAssetClick: (AssetId) -> Unit,
    onAssetHide: (AssetId) -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Box {
                        TextButton(onClick = onShowWallets) {
                            Row(verticalAlignment = Alignment.CenterVertically ) {
                                AsyncImage(
                                    model = walletInfo.icon.ifEmpty {
                                        "android.resource://com.gemwallet.android/drawable/ic_splash"
                                    },
                                    contentDescription = "icon",
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
                    IconButton(onClick = onShowAssetManage) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = "asset_manager",
                        )
                    }
                }
            )
        }
    ) {
        AssetListPushToRefresh(
            modifier = Modifier.padding(top = it.calculateTopPadding()),
            isLoading = isLoading,
            walletInfo = walletInfo,
            assets = assets,
            transactions = transactions,
            swapEnabled = swapEnabled,
            onRefresh = onRefresh,
            onShowAssetManage = onShowAssetManage,
            onSendClick = onSendClick,
            onReceiveClick = onReceiveClick,
            onBuyClick = onBuyClick,
            onSwapClick = onSwapClick,
            onTransactionClick = onTransactionClick,
            onAssetClick = onAssetClick,
            onAssetHide = onAssetHide,
            listState = listState,
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AssetListPushToRefresh(
    isLoading: Boolean,
    walletInfo: WalletInfoUIState,
    assets: ImmutableList<AssetUIState>,
    transactions: ImmutableList<TransactionExtended>,
    swapEnabled: Boolean,
    onRefresh: () -> Unit,
    onShowAssetManage: () -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onBuyClick: () -> Unit,
    onSwapClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onAssetClick: (AssetId) -> Unit,
    onAssetHide: (AssetId) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val pullRefreshState = rememberPullRefreshState(isLoading, { onRefresh() })

    Box(
        modifier = modifier.pullRefresh(pullRefreshState),
    ) {
        AssetList(
            walletInfo = walletInfo,
            assets = assets,
            transactions = transactions,
            swapEnabled = swapEnabled,
            onShowAssetManage = onShowAssetManage,
            onSendClick = onSendClick,
            onReceiveClick = onReceiveClick,
            onBuyClick = onBuyClick,
            onSwapClick = onSwapClick,
            onTransactionClick = onTransactionClick,
            onAssetClick = onAssetClick,
            onAssetHide = onAssetHide,
            listState = listState,
        )
        PullRefreshIndicator(isLoading, pullRefreshState, Modifier.align(Alignment.TopCenter))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AssetList(
    walletInfo: WalletInfoUIState,
    assets: ImmutableList<AssetUIState>,
    transactions: ImmutableList<TransactionExtended>,
    swapEnabled: Boolean,
    onShowAssetManage: () -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onBuyClick: () -> Unit,
    onSwapClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onAssetClick: (AssetId) -> Unit,
    onAssetHide: (AssetId) -> Unit,
    listState: LazyListState,
) {
    val clipboardManager = LocalClipboardManager.current
    var longPressedAsset by remember {
        mutableStateOf<AssetId?>(null)
    }
    LazyColumn(
        state = listState,
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
                        onSwap = if (swapEnabled) onSwapClick else null,
                    )
                }
            )
        }
        transactionsList(transactions) { onTransactionClick(it) }
        if (transactions.isNotEmpty()) {
            item {
                Spacer16()
                HorizontalDivider(thickness = 0.4.dp)
            }
        }
        items(items = assets, key = { it.id.toIdentifier() }) { asset->
            var itemWidth by remember { mutableIntStateOf(0) }
            Box(
                modifier = Modifier.onSizeChanged { itemWidth = it.width }
            ) {
                AssetListItem(
                    chain = asset.id.chain,
                    title = asset.name,
                    iconUrl = asset.icon,
                    value = asset.value,
                    assetType = asset.type,
                    isZeroValue = asset.isZeroValue,
                    fiatAmount = asset.fiat,
                    price = asset.price,
                    modifier = Modifier.combinedClickable(
                        onClick = { onAssetClick(asset.id) },
                        onLongClick = { longPressedAsset = asset.id },
                    )
                )
                DropdownMenu(
                    expanded = longPressedAsset == asset.id,
                    offset = DpOffset((with(LocalDensity.current) { itemWidth.toDp() } / 2), 8.dp),
                    onDismissRequest = { longPressedAsset = null },
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = R.string.wallet_copy_address),
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "address_copy"
                            )
                        },
                        onClick = {
                            clipboardManager.setText(AnnotatedString(asset.owner))
                            longPressedAsset = null
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.common_hide)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "wallet_config"
                            )
                        },
                        onClick = {
                            onAssetHide(asset.id)
                            longPressedAsset = null
                        }
                    )
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .clickable(onClick = onShowAssetManage)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
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
            Spacer16()
        }
    }
}

@Composable
@Preview
fun PreviewAssetsList() {
    WalletTheme {
        AssetList(
            assets = listOf(
                AssetUIState(
                    id = AssetId(Chain.Bitcoin),
                    name = "Bitcoin",
                    icon = AssetId(Chain.Bitcoin).getIconUrl(),
                    type = AssetType.NATIVE,
                    value = "0.9 BTC",
                    isZeroValue = true,
                    price = PriceUIState("0.0000000001$", PriceState.Down, "0,5%"),
                    fiat = "90 000$",
                    owner = "",
                    symbol = "BTC",
                )
            ).toImmutableList(),
            transactions = emptyList<TransactionExtended>().toImmutableList(),
            walletInfo = WalletInfoUIState(
                name = "Foo Wallet Name #1",
                totalValue = "90 000$",
                type = WalletType.multicoin,
            ),
            onShowAssetManage = {},
            onSendClick = {},
            onReceiveClick = {},
            onBuyClick = {},
            onSwapClick = {},
            onTransactionClick = {},
            onAssetClick = {},
            onAssetHide = {},
            swapEnabled = false,
            listState = rememberLazyListState(),
        )
    }
}