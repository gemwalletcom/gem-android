package com.gemwallet.android.features.asset.details.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset.details.models.AssetInfoUIModel
import com.gemwallet.android.features.asset.details.models.AssetInfoUIState
import com.gemwallet.android.features.asset.details.models.AssetStateError
import com.gemwallet.android.features.asset.details.viewmodels.AsseDetailsViewModel
import com.gemwallet.android.features.banners.views.BannersScene
import com.gemwallet.android.features.transactions.components.transactionsList
import com.gemwallet.android.interactors.chain
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.ui.components.AmountListHead
import com.gemwallet.android.ui.components.AssetHeadActions
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.SubheaderItem
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.open
import com.gemwallet.android.ui.components.priceColor
import com.gemwallet.android.ui.theme.padding32
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.BannerEvent
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType
import uniffi.gemstone.Config
import uniffi.gemstone.DocsUrl

@Composable
fun AssetDetailsScene(
    assetId: AssetId,
    onCancel: () -> Unit,
    onTransfer: (AssetId) -> Unit,
    onReceive: (AssetId) -> Unit,
    onBuy: (AssetId) -> Unit,
    onSwap: (AssetId, AssetId?) -> Unit,
    onTransaction: (txId: String) -> Unit,
    onChart: (AssetId) -> Unit,
    onStake: (AssetId) -> Unit,
) {
    uniffi.gemstone.AssetWrapper
    val viewModel: AsseDetailsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiModel by viewModel.uiModel.collectAsStateWithLifecycle()

    when {
        uiState is AssetInfoUIState.Fatal -> FatalStateScene(
            title = "Asset",
            message = when ((uiState as AssetInfoUIState.Fatal).error) {
                AssetStateError.AssetNotFound -> "Asset not found"
            },
            onCancel = onCancel
        )
        uiState is AssetInfoUIState.Idle && uiModel != null -> Success(
            uiState = uiModel ?: return,
            syncState = (uiState as AssetInfoUIState.Idle).sync,
            onRefresh = viewModel::refresh,
            onTransfer = onTransfer,
            onBuy = onBuy,
            onSwap = onSwap,
            onReceive = onReceive,
            onTransaction = onTransaction,
            onChart = onChart,
            onStake = onStake,
            onPriceAlert = viewModel::enablePriceAlert,
            onCancel = onCancel,
        )
        uiState is AssetInfoUIState.Loading || uiModel == null -> LoadingScene(assetId.chain.string, onCancel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Success(
    uiState: AssetInfoUIModel,
    syncState: AssetInfoUIState.SyncState,
    onRefresh: () -> Unit,
    onCancel: () -> Unit,
    onTransfer: (AssetId) -> Unit,
    onReceive: (AssetId) -> Unit,
    onBuy: (AssetId) -> Unit,
    onSwap: (AssetId, AssetId?) -> Unit,
    onTransaction: (txId: String) -> Unit,
    onChart: (AssetId) -> Unit,
    onStake: (AssetId) -> Unit,
    onPriceAlert: (AssetId) -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    Scene(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = uiState.name, maxLines = 1)
            }
        },
        actions = {
            IconButton(
                onClick = {
                    onPriceAlert(uiState.asset.id)
                }
            ) {
                if (uiState.priceAlertEnabled) {
                    Icon(Icons.Default.Notifications, "")
                } else {
                    Icon(Icons.Default.NotificationsNone, "")
                }
            }
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(uiState.account.owner))
                }
            ) {
                Icon(Icons.Default.ContentCopy, "")
            }
        },
        onClose = onCancel,
        contentPadding = PaddingValues(0.dp)
    ) {
        val isRefreshing = syncState == AssetInfoUIState.SyncState.Loading
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    containerColor = MaterialTheme.colorScheme.background
                )
            }
        ) {
            LazyColumn {
                item {
                    AmountListHead(
                        amount = uiState.account.totalBalance,
                        equivalent = uiState.account.totalFiat,
                        iconUrl = uiState.iconUrl,
                        supportIconUrl = if (uiState.asset.id.type() == AssetSubtype.NATIVE) null else uiState.asset.chain().getIconUrl(),
                        placeholder = uiState.asset.name.getOrNull(0)?.toString() ?: uiState.asset.type.string,
                    ) {
                        AssetHeadActions(
                            walletType = uiState.account.walletType,
                            onTransfer = { onTransfer(uiState.asset.id) },
                            transferEnabled = uiState.account.walletType != WalletType.view,
                            onReceive = { onReceive(uiState.asset.id) },
                            onBuy = if (uiState.isBuyEnabled) {
                                { onBuy(uiState.asset.id) }
                            } else {
                                null
                            },
                            onSwap = if (uiState.isSwapEnabled && uiState.account.walletType != WalletType.view) {
                                {
                                    val toAssetId = if (uiState.asset.type == AssetType.NATIVE) {
                                        null
                                    } else {
                                        uiState.asset.id.chain.asset().id
                                    }
                                    onSwap(uiState.asset.id, toAssetId)
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
                item {
                    BannersScene(
                        asset = uiState.asset,
                        onClick = {
                            when (it.event) {
                                BannerEvent.Stake -> onStake(uiState.asset.id)
                                BannerEvent.AccountBlockedMultiSignature ->
                                    uriHandler.open(Config().getDocsUrl(DocsUrl.TRON_MULTI_SIGNATURE))
                                else -> {}
                            }
                        },
                        false
                    )
                    HorizontalDivider(thickness = 0.dp)
                }
                networkInfo(uiState, onChart)
                balanceDetails(uiState, onStake)
                if (uiState.transactions.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = padding32)) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.secondary,
                                text = stringResource(R.string.activity_empty_state_message)
                            )
                        }
                    }
                }
                transactionsList(uiState.transactions, onTransaction)
            }
        }
    }
}

private fun LazyListScope.networkInfo(
    uiState: AssetInfoUIModel,
    onChart: (AssetId) -> Unit,
) {
    val cells = mutableListOf<CellEntity<Any>>()
    item {
        if (uiState.priceValue.isNotEmpty()) {
            cells.add(
                CellEntity(
                    label = stringResource(id = R.string.asset_price),
                    data = uiState.priceValue,
                    trailing = {
                        Text(
                            text = uiState.priceDayChanges,
                            color = priceColor(uiState.priceChangedType),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    action = { onChart(uiState.asset.id) },
                    testTag = "assetChart"
                )
            )
        }
        if (uiState.tokenType != AssetType.NATIVE) {
            cells.add(
                CellEntity(
                    label = stringResource(id = R.string.transfer_network),
                    data = uiState.networkTitle,
                    trailing = {
                        AsyncImage(
                            modifier = Modifier.size(20.dp),
                            model = uiState.networkIcon,
                            placeholderText = uiState.tokenType.string,
                            contentDescription = "asset_icon"
                        )
                    },
                )
            )
        }
        if (cells.isNotEmpty()) {
            Table(items = cells)
        }
    }
}

private fun LazyListScope.balanceDetails(
    uiState: AssetInfoUIModel,
    onStake: (AssetId) -> Unit,
) {
    if (!uiState.account.hasBalanceDetails) {
        return
    }
    item {
        val uriHandler = LocalUriHandler.current

        if (uiState.account.available.isNotEmpty()) {
            Spacer(modifier = Modifier.size(8.dp))
            SubheaderItem(title = stringResource(id = R.string.asset_balances))
            Spacer(modifier = Modifier.size(8.dp))
        }
        val cells = mutableListOf<CellEntity<Any>>()
        if (uiState.account.available.isNotEmpty()) {
            cells.add(
                CellEntity(
                    label = stringResource(R.string.asset_balances_available),
                    data = uiState.account.available,
                )
            )
        }
        if (uiState.account.stake.isNotEmpty()) {
            cells.add(
                CellEntity(
                    label = stringResource(R.string.wallet_stake),
                    data = uiState.account.stake,
                    action = { onStake(uiState.asset.id) },
                    testTag = "assetStake",
                )
            )
        }
        if (uiState.account.reserved.isNotEmpty()) {
            cells.add(
                CellEntity(
                    label = stringResource(R.string.asset_balances_reserved),
                    data = uiState.account.reserved,
                    action = {
                        if (uiState.asset.id.chain == Chain.Xrp) {
                            uriHandler.open("https://xrpl.org/docs/concepts/accounts/reserves")
                        }
                    },
                )
            )
        }
        Table(cells)
    }
}