package com.gemwallet.android.features.asset.details.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset.details.models.AssetInfoUIModel
import com.gemwallet.android.features.asset.details.models.AssetInfoUIState
import com.gemwallet.android.features.asset.details.models.AssetStateError
import com.gemwallet.android.features.asset.details.viewmodels.AssetInfoViewModel
import com.gemwallet.android.features.transactions.components.transactionsList
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.ui.components.AmountListHead
import com.gemwallet.android.ui.components.AssetHeadActions
import com.gemwallet.android.ui.components.AsyncImage
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.LoadingScene
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.SubheaderItem
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.priceColor
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType

@Composable
fun AssetDetailsScene(
    assetId: AssetId,
    onCancel: () -> Unit,
    onTransfer: (AssetId) -> Unit,
    onReceive: (AssetId) -> Unit,
    onBuy: (AssetId) -> Unit,
    onSwap: (AssetId) -> Unit,
    onTransaction: (txId: String) -> Unit,
    onChart: (AssetId) -> Unit,
    onStake: (AssetId) -> Unit,
) {
    val viewModel: AssetInfoViewModel = hiltViewModel()
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
            onCancel = onCancel,
        )
        uiState is AssetInfoUIState.Loading || uiModel == null -> LoadingScene(assetId.chain.string, onCancel)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Success(
    uiState: AssetInfoUIModel,
    syncState: AssetInfoUIState.SyncState,
    onRefresh: () -> Unit,
    onCancel: () -> Unit,
    onTransfer: (AssetId) -> Unit,
    onReceive: (AssetId) -> Unit,
    onBuy: (AssetId) -> Unit,
    onSwap: (AssetId) -> Unit,
    onTransaction: (txId: String) -> Unit,
    onChart: (AssetId) -> Unit,
    onStake: (AssetId) -> Unit,
) {
    Scene(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = uiState.name, maxLines = 1)
            }
        },
        onClose = onCancel,
        contentPadding = PaddingValues(0.dp)
    ) {
        val pullRefreshState = rememberPullRefreshState(
            syncState != AssetInfoUIState.SyncState.None, { onRefresh() },
        )

        Box(
            modifier = Modifier.pullRefresh(pullRefreshState),
        ) {
            LazyColumn {
                item {
                    AmountListHead(
                        amount = uiState.account.totalBalance,
                        equivalent = uiState.account.totalFiat,
                        iconUrl = uiState.iconUrl,
                        supportIconUrl = if (uiState.assetId.type() == AssetSubtype.NATIVE) null else uiState.assetId.chain.getIconUrl(),
                        placeholder = uiState.tokenType.string,
                    ) {
                        AssetHeadActions(
                            walletType = uiState.account.walletType,
                            onTransfer = { onTransfer(uiState.assetId) },
                            transferEnabled = uiState.account.walletType != WalletType.view,
                            onReceive = { onReceive(uiState.assetId) },
                            onBuy = if (uiState.isBuyEnabled) {
                                { onBuy(uiState.assetId) }
                            } else {
                                null
                            },
                            onSwap = if (uiState.isSwapEnabled && uiState.account.walletType != WalletType.view) {
                                { onSwap(uiState.assetId) }
                            } else {
                                null
                            },
                        )
                    }
                }
                networkInfo(uiState, onChart)
                balanceDetails(uiState, onStake)
                transactionsList(uiState.transactions, onTransaction)
            }
            PullRefreshIndicator(
                modifier = Modifier.align(Alignment.TopCenter),
                refreshing = syncState != AssetInfoUIState.SyncState.None,
                state = pullRefreshState,
            )
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
                    action = { onChart(uiState.assetId) },
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
                    action = { onStake(uiState.assetId) },
                )
            )
        }
        if (uiState.account.reserved.isNotEmpty()) {
            cells.add(
                CellEntity(
                    label = stringResource(R.string.asset_balances_reserved),
                    data = uiState.account.reserved,
                    action = {
                        if (uiState.assetId.chain == Chain.Xrp) {
                            uriHandler.openUri("https://xrpl.org/docs/concepts/accounts/reserves")
                        }
                    },
                )
            )
        }
        Table(cells)
    }
}