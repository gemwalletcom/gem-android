package com.gemwallet.features.asset.presents.details.views

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ext.getReserveBalanceUrl
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.TransactionExtended
import com.gemwallet.android.ui.components.list_item.transactionsList
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.gemwallet.android.ui.models.getListPosition
import com.gemwallet.android.ui.open
import com.gemwallet.features.asset.presents.details.views.components.AssetDetailsMenu
import com.gemwallet.features.asset.presents.details.views.components.AssetHeadItem
import com.gemwallet.features.asset.presents.details.views.components.BalancePropertyItem
import com.gemwallet.features.asset.presents.details.views.components.BannerItem
import com.gemwallet.features.asset.presents.details.views.components.EmptyTransactionsItem
import com.gemwallet.features.asset.presents.details.views.components.balancesHeader
import com.gemwallet.features.asset.presents.details.views.components.network
import com.gemwallet.features.asset.presents.details.views.components.price
import com.gemwallet.features.asset.presents.details.views.components.status
import com.gemwallet.features.asset.viewmodels.details.models.AssetInfoUIModel
import com.gemwallet.features.asset.viewmodels.details.models.AssetInfoUIState
import com.wallet.core.primitives.AssetId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AssetDetailsScene(
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
    isOperationEnabled: Boolean,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val snackBar = remember { SnackbarHostState() }

    Scene(
        titleContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = uiState.name,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis
                )
            }
        },
        actions = {
            AssetDetailsMenu(
                uiState = uiState,
                priceAlertEnabled = priceAlertEnabled,
                snackBar = snackBar,
                onPriceAlert = onPriceAlert,
            )
        },
        onClose = onCancel,
        contentPadding = PaddingValues(0.dp),
        snackbar = snackBar,
    ) {
        val isRefreshing = syncState == AssetInfoUIState.SyncState.Loading

        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    containerColor = MaterialTheme.colorScheme.background
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    AssetHeadItem(
                        uiState = uiState,
                        isOperationEnabled = isOperationEnabled,
                        onTransfer = onTransfer,
                        onReceive = onReceive,
                        onBuy = onBuy,
                        onSwap = onSwap
                    )
                }
                item { BannerItem(uiState.assetInfo, onStake, onConfirm) }
                status(uiState.asset, uiState.assetInfo.rank)
                price(uiState, onChart)
                network(uiState, openNetwork)
                balancesHeader(uiState.accountInfoUIModel)
                itemsIndexed(uiState.accountInfoUIModel.balances) { index, item ->
                    BalancePropertyItem(
                        title = item.type.label,
                        balance = item.value,
                        listPosition = uiState.accountInfoUIModel.balances.getListPosition(index),
                        onAction = when (item.type) {
                            AssetInfoUIModel.BalanceViewType.Available -> null
                            AssetInfoUIModel.BalanceViewType.Stake -> {
                                { onStake(uiState.asset.id) }
                            }
                            AssetInfoUIModel.BalanceViewType.Reserved -> {
                                {
                                    uiState.asset.id.chain.getReserveBalanceUrl()
                                        ?.let { uriHandler.open(context, it) }
                                }
                            }
                        }
                    )
                }
                item { EmptyTransactionsItem(transactions.size) }
                transactionsList(transactions, onTransaction)
            }
        }
    }
}