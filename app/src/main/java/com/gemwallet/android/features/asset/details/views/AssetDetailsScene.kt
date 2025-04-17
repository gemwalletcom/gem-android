package com.gemwallet.android.features.asset.details.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.chain
import com.gemwallet.android.ext.getReserveBalanceUrl
import com.gemwallet.android.ext.networkAsset
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset.details.models.AssetInfoUIModel
import com.gemwallet.android.features.asset.details.models.AssetInfoUIState
import com.gemwallet.android.features.asset.details.models.AssetStateError
import com.gemwallet.android.features.asset.details.viewmodels.AsseDetailsViewModel
import com.gemwallet.android.features.banners.views.BannersScene
import com.gemwallet.android.features.transactions.components.transactionsList
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.TransactionExtended
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.AmountListHead
import com.gemwallet.android.ui.components.AssetHeadActions
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.designsystem.padding32
import com.gemwallet.android.ui.components.designsystem.trailingIconMedium
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.image.getIconUrl
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.open
import com.gemwallet.android.ui.components.priceColor
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.BannerEvent
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.launch
import uniffi.gemstone.Config
import uniffi.gemstone.DocsUrl

@Composable
fun AssetDetailsScene(
    assetId: AssetId,
    onCancel: () -> Unit,
    onTransfer: AssetIdAction,
    onReceive: (AssetId) -> Unit,
    onBuy: (AssetId) -> Unit,
    onSwap: (AssetId, AssetId?) -> Unit,
    onTransaction: (txId: String) -> Unit,
    onChart: (AssetId) -> Unit,
    openNetwork: AssetIdAction,
    onStake: (AssetId) -> Unit,
    onConfirm: (ConfirmParams) -> Unit
) {
    val viewModel: AsseDetailsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val priceAlertEnabled by viewModel.priceAlertEnabled.collectAsStateWithLifecycle()
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
            transactions = transactions,
            priceAlertEnabled = priceAlertEnabled,
            syncState = (uiState as AssetInfoUIState.Idle).sync,
            onRefresh = viewModel::refresh,
            onTransfer = onTransfer,
            onBuy = onBuy,
            onSwap = onSwap,
            onReceive = onReceive,
            onTransaction = onTransaction,
            onChart = onChart,
            openNetwork = openNetwork,
            onStake = onStake,
            onPriceAlert = viewModel::enablePriceAlert,
            onConfirm = onConfirm,
            onCancel = onCancel,
        )
        uiState is AssetInfoUIState.Loading || uiModel == null -> LoadingScene(assetId.chain.string, onCancel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Success(
    uiState: AssetInfoUIModel,
    transactions: List<TransactionExtended>,
    priceAlertEnabled: Boolean,
    syncState: AssetInfoUIState.SyncState,
    onRefresh: () -> Unit,
    onCancel: () -> Unit,
    onTransfer: AssetIdAction,
    onReceive: (AssetId) -> Unit,
    onBuy: (AssetId) -> Unit,
    onSwap: (AssetId, AssetId?) -> Unit,
    onTransaction: (txId: String) -> Unit,
    onChart: (AssetId) -> Unit,
    openNetwork: AssetIdAction,
    onStake: (AssetId) -> Unit,
    onPriceAlert: (AssetId) -> Unit,
    onConfirm: (ConfirmParams) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val pullToRefreshState = rememberPullToRefreshState()
    val clipboardManager = LocalClipboard.current.nativeClipboard
    val uriHandler = LocalUriHandler.current

    val snackbar = remember { SnackbarHostState() }
    val priceAlertToastRes = if (priceAlertEnabled) R.string.price_alerts_disabled_for else R.string.price_alerts_enabled_for
    val priceAlertToastMessage = stringResource(priceAlertToastRes, uiState.asset.name)

    Scene(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = uiState.name, maxLines = 1, overflow = TextOverflow.MiddleEllipsis)
            }
        },
        actions = {
            IconButton(
                onClick = {
                    onPriceAlert(uiState.asset.id)
                    scope.launch { snackbar.showSnackbar(message = priceAlertToastMessage) }
                }
            ) {
                if (priceAlertEnabled) {
                    Icon(Icons.Default.Notifications, "")
                } else {
                    Icon(Icons.Default.NotificationsNone, "")
                }
            }
            IconButton(
                onClick = { clipboardManager.setPlainText(uiState.accountInfoUIModel.owner) }
            ) {
                Icon(Icons.Default.ContentCopy, "")
            }
        },
        onClose = onCancel,
        contentPadding = PaddingValues(0.dp),
        snackbar = snackbar,
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
                        amount = uiState.accountInfoUIModel.totalBalance,
                        equivalent = uiState.accountInfoUIModel.totalFiat,
                        iconUrl = uiState.iconUrl,
                        supportIconUrl = if (uiState.asset.id.type() == AssetSubtype.NATIVE) null else uiState.asset.chain().getIconUrl(),
                        placeholder = uiState.asset.name.getOrNull(0)?.toString() ?: uiState.asset.type.string,
                    ) {
                        AssetHeadActions(
                            walletType = uiState.accountInfoUIModel.walletType,
                            onTransfer = { onTransfer(uiState.asset.id) },
                            transferEnabled = uiState.accountInfoUIModel.walletType != WalletType.view,
                            onReceive = { onReceive(uiState.asset.id) },
                            onBuy = if (uiState.isBuyEnabled) {
                                { onBuy(uiState.asset.id) }
                            } else {
                                null
                            },
                            onSwap = if (uiState.isSwapEnabled && uiState.accountInfoUIModel.walletType != WalletType.view) {
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
                                BannerEvent.ActivateAsset -> {
                                    val params = ConfirmParams.Builder(
                                        asset = uiState.asset,
                                        from = uiState.assetInfo.owner ?: return@BannersScene
                                    ).activate()
                                    onConfirm(params)
                                }
                                BannerEvent.AccountActivation -> uiState.asset.chain()
                                    .getReserveBalanceUrl()?.let { uriHandler.open(it) }
                                else -> {}
                            }
                        },
                        false
                    )
                    HorizontalDivider(thickness = 0.dp)
                }
                networkInfo(uiState, onChart, openNetwork)
                balanceDetails(uiState, onStake)
                if (transactions.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = padding32)) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.secondary,
                                text = stringResource(R.string.asset_state_empty_title)
                            )
                            // TODO: Add empty description
                        }
                    }
                }
                transactionsList(transactions, onTransaction)
            }
        }
    }
}

private fun LazyListScope.networkInfo(
    uiState: AssetInfoUIModel,
    onChart: (AssetId) -> Unit,
    openNetwork: AssetIdAction,
) {
    item {
        val cells = mutableListOf<CellEntity<Any>>()
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
        if (uiState.tokenType != AssetType.NATIVE) {
            cells.add(
                CellEntity(
                    label = stringResource(id = R.string.transfer_network),
                    data = uiState.networkTitle,
                    trailing = { AsyncImage(uiState.asset.networkAsset(), trailingIconMedium) },
                    action = { openNetwork(AssetId(uiState.asset.chain())) },
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
    if (!uiState.accountInfoUIModel.hasBalanceDetails) {
        return
    }
    item {
        val uriHandler = LocalUriHandler.current

        if (uiState.accountInfoUIModel.available.isNotEmpty()) {
            Spacer8()
            SubheaderItem(title = stringResource(id = R.string.asset_balances))
            Spacer8()
        }
        val cells = mutableListOf<CellEntity<Any>>()
        if (uiState.accountInfoUIModel.available.isNotEmpty()) {
            cells.add(
                CellEntity(
                    label = stringResource(R.string.asset_balances_available),
                    data = uiState.accountInfoUIModel.available,
                )
            )
        }
        if (uiState.accountInfoUIModel.stake.isNotEmpty()) {
            cells.add(
                CellEntity(
                    label = stringResource(R.string.wallet_stake),
                    data = uiState.accountInfoUIModel.stake,
                    action = { onStake(uiState.asset.id) },
                    testTag = "assetStake",
                )
            )
        }
        if (uiState.accountInfoUIModel.reserved.isNotEmpty()) {
            cells.add(
                CellEntity(
                    label = stringResource(R.string.asset_balances_reserved),
                    data = uiState.accountInfoUIModel.reserved,
                    action = {
                        uriHandler.open(uiState.asset.id.chain.getReserveBalanceUrl() ?: return@CellEntity)
                    },
                )
            )
        }
        Table(cells)
    }
}