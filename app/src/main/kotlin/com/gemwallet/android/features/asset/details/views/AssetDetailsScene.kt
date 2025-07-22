package com.gemwallet.android.features.asset.details.views

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.BuildConfig
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.chain
import com.gemwallet.android.ext.getReserveBalanceUrl
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset.details.models.AssetInfoUIModel
import com.gemwallet.android.features.asset.details.models.AssetInfoUIState
import com.gemwallet.android.features.asset.details.models.AssetStateError
import com.gemwallet.android.features.asset.details.viewmodels.AsseDetailsViewModel
import com.gemwallet.android.features.banners.views.BannersScene
import com.gemwallet.android.features.activities.presents.components.transactionsList
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.TransactionExtended
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_head.AmountListHead
import com.gemwallet.android.ui.components.list_head.AssetHeadActions
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.padding32
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.property.PropertyNetwork
import com.gemwallet.android.ui.components.list_item.priceColor
import com.gemwallet.android.ui.components.screen.FatalStateScene
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.gemwallet.android.ui.open
import com.gemwallet.android.ui.theme.pendingColor
import com.wallet.core.primitives.Asset
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
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current.nativeClipboard
    val uriHandler = LocalUriHandler.current

    val snackBar = remember { SnackbarHostState() }
    val priceAlertToastRes = if (priceAlertEnabled) R.string.price_alerts_disabled_for else R.string.price_alerts_enabled_for
    val priceAlertToastMessage = stringResource(priceAlertToastRes, uiState.asset.name)
    var menuExpanded by remember { mutableStateOf(false) }
    val shareTitle = stringResource(id = R.string.common_share)

    val onShare = fun () {
        val type = "text/plain"
        val subject = "${uiState.assetInfo.owner?.chain}\n${uiState.assetInfo.asset.symbol}"

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = type
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, uiState.assetInfo.owner?.address)

        context.startActivity(Intent.createChooser(intent, shareTitle))
    }

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
                    scope.launch { snackBar.showSnackbar(message = priceAlertToastMessage) }
                }
            ) {
                if (priceAlertEnabled) {
                    Icon(Icons.Default.Notifications, "")
                } else {
                    Icon(Icons.Default.NotificationsNone, "")
                }
            }
            if (BuildConfig.DEBUG) {
                IconButton(
                    onClick = {
                        clipboardManager.setPlainText(
                            context,
                            uiState.accountInfoUIModel.owner
                        )
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, "")
                }
            }
            IconButton(onClick = { menuExpanded = !menuExpanded }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More",
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                uiState.explorerAddressUrl?.let {
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(R.string.asset_view_address_on, uiState.explorerName))
                        },
                        onClick = { uriHandler.open(context, it) },
                    )

                }
                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.common_share))
                    },
                    onClick = onShare,
                )
            }
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
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    containerColor = MaterialTheme.colorScheme.background
                )
            }
        ) {
            LazyColumn {
                head(uiState, onTransfer, onReceive, onBuy, onSwap)
                banner(uiState.assetInfo, onStake, onConfirm)
                status(uiState.asset, uiState.assetInfo.rank)
                price(uiState, onChart)
                network(uiState, openNetwork)
                balancesHeader(uiState.accountInfoUIModel)
                availableBalance(uiState.accountInfoUIModel.available)
                additionBalance(R.string.wallet_stake, uiState.accountInfoUIModel.stake) {
                    onStake(uiState.asset.id)
                }
                additionBalance(R.string.asset_balances_reserved, uiState.accountInfoUIModel.reserved) {
                    uiState.asset.id.chain.getReserveBalanceUrl()?.let { uriHandler.open(context, it) }
                }
                if (transactions.isEmpty()) {
                    item {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = padding32)) {
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

private fun LazyListScope.head(
    uiState: AssetInfoUIModel,
    onTransfer: AssetIdAction,
    onReceive: (AssetId) -> Unit,
    onBuy: (AssetId) -> Unit,
    onSwap: (AssetId, AssetId?) -> Unit,
) {
    item {
        AmountListHead(
            amount = uiState.accountInfoUIModel.totalBalance,
            equivalent = uiState.accountInfoUIModel.totalFiat,
            icon = uiState.asset,
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
}

private fun LazyListScope.banner(
    assetInfo: AssetInfo,
    onStake: (AssetId) -> Unit,
    onConfirm: (ConfirmParams) -> Unit
) {
    item {
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        BannersScene(
            asset = assetInfo.asset,
            onClick = {
                when (it.event) {
                    BannerEvent.Stake -> onStake(assetInfo.asset.id)
                    BannerEvent.AccountBlockedMultiSignature ->
                        uriHandler.open(context, Config().getDocsUrl(DocsUrl.TRON_MULTI_SIGNATURE))
                    BannerEvent.ActivateAsset -> {
                        val params = ConfirmParams.Builder(
                            asset = assetInfo.asset,
                            from = assetInfo.owner ?: return@BannersScene
                        ).activate()
                        onConfirm(params)
                    }
                    BannerEvent.AccountActivation -> assetInfo.asset.chain()
                        .getReserveBalanceUrl()?.let { uri -> uriHandler.open(context, uri) }
                    else -> {}
                }
            },
            false
        )
        HorizontalDivider(thickness = 0.dp)
    }
}

private fun LazyListScope.status(asset: Asset, rank: Int) {
    val status = rank.getVerificationStatus()
    if (asset.id.type() == AssetSubtype.NATIVE || status == null) {
        return
    }
    item {
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        PropertyItem(
            modifier = Modifier.clickable { uriHandler.open(context, Config().getDocsUrl(DocsUrl.TOKEN_VERIFICATION)) },
            title = {
                PropertyTitleText(
                    text = R.string.transaction_status,
                    info = when (status) {
                        AssetVerification.Suspicious -> InfoSheetEntity.AssetStatusSuspiciousInfo
                        AssetVerification.Unverified -> InfoSheetEntity.AssetStatusUnverifiedInfo
                    }
                )
            },
            data = {
                PropertyDataText(
                    stringResource(
                        when (status) {
                            AssetVerification.Suspicious ->  R.string.asset_verification_suspicious
                            AssetVerification.Unverified -> R.string.asset_verification_unverified
                        }
                    ),
                    color = when (status) {
                        AssetVerification.Suspicious -> MaterialTheme.colorScheme.error
                        AssetVerification.Unverified -> pendingColor
                    },
                    badge = {
                        DataBadgeChevron(
                            when (status) {
                                AssetVerification.Suspicious -> R.drawable.suspicious
                                AssetVerification.Unverified -> R.drawable.unverified
                            }
                        )
                    }
                )
            },
        )
        Spacer16()
    }
}

private fun LazyListScope.price(
    uiState: AssetInfoUIModel,
    onChart: (AssetId) -> Unit,
) {
    item {
        PropertyItem(
            modifier = Modifier.clickable { onChart(uiState.asset.id) }
                .testTag("assetChart"),
            title = { PropertyTitleText(R.string.asset_price) },
            data = {
                PropertyDataText(
                    text = uiState.priceValue,
                    badge = {
                        DataBadgeChevron {
                            Text(
                                text = uiState.priceDayChanges,
                                color = priceColor(uiState.priceChangedType),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                )
            }
        )
    }
}

private fun LazyListScope.network(
    uiState: AssetInfoUIModel,
    openNetwork: AssetIdAction,
) {
    if (uiState.tokenType == AssetType.NATIVE) {
        return
    }
    item { PropertyNetwork(uiState.asset, openNetwork) }
}

private fun LazyListScope.balancesHeader(model: AssetInfoUIModel.AccountInfoUIModel) {
    if (model.available.isEmpty() && model.stake.isEmpty() && model.reserved.isEmpty()) {
        return
    }
    item {
        SubheaderItem(title = stringResource(id = R.string.asset_balances))
    }
}

private fun LazyListScope.availableBalance(balance: String?) {
    if (balance.isNullOrEmpty()) {
        return
    }
    item {
        PropertyItem(R.string.asset_balances_available, balance)
    }
}

private fun LazyListScope.additionBalance(
    @StringRes title: Int,
    balance: String?,
    onAction:() -> Unit,
) {
    if (balance.isNullOrEmpty()) {
        return
    }
    item {
        PropertyItem(
            modifier = Modifier.clickable(onClick = onAction).testTag("assetStake"),
            title = { PropertyTitleText(title) },
            data = { PropertyDataText(balance, badge = { DataBadgeChevron() }) },
        )
    }
}

private enum class AssetVerification(val min: Int) {
    Suspicious(5),
    Unverified(15),
}

private fun Int.getVerificationStatus(): AssetVerification? {
    return if (this < AssetVerification.Suspicious.min) {
        AssetVerification.Suspicious
    } else if (this < AssetVerification.Unverified.min) {
        AssetVerification.Unverified
    } else {
        null
    }
}