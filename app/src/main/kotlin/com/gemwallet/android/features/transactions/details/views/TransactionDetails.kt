package com.gemwallet.android.features.transactions.details.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.features.transactions.details.model.TxDetailsScreenModel
import com.gemwallet.android.features.transactions.details.viewmodels.TransactionDetailsViewModel
import com.gemwallet.android.features.transactions.details.viewmodels.getIcon
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.AmountListHead
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.NftHead
import com.gemwallet.android.ui.components.SwapListHead
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.designsystem.trailingIconMedium
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.image.getSupportIconUrl
import com.gemwallet.android.ui.components.list_item.PropertyDataText
import com.gemwallet.android.ui.components.list_item.PropertyItem
import com.gemwallet.android.ui.components.list_item.PropertyTitleText
import com.gemwallet.android.ui.components.open
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.components.titles.getTransactionTitle
import com.gemwallet.android.ui.theme.pendingColor
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.SwapProvider
import com.wallet.core.primitives.TransactionDirection
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType

@Composable
fun TransactionDetails(
    onCancel: () -> Unit,
    viewModel: TransactionDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.screenModel.collectAsStateWithLifecycle()
    val model = uiState

    if (model == null) {
        LoadingScene(title = "", onCancel)
    } else {
        Scene(
            title = model.type.getTransactionTitle(model.direction, model.state),
            onClose = onCancel,
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                transactionItemHead(model)
                transactionDateItem(model.createdAt)
                transactionRecipientItem(model)
                transactionStatusItem(model)
                transactionMemoItem(model.memo)
                transactionNetworkItem(model.networkTitle, model.assetId)
                transactionProviderItem(model.provider)
                transactionNetworkFeeItem(model.networkTitle, model.feeCrypto, model.feeFiat)
                transactionExplorer(model.explorerName, model.explorerUrl)
            }
        }
    }
}

private fun LazyListScope.transactionItemHead(model: TxDetailsScreenModel) {
    item {
        when (model.type) {
            TransactionType.Swap -> SwapListHead(
                fromAsset = model.fromAsset,
                fromValue = model.fromValue ?: "0",
                toAsset = model.toAsset,
                toValue = model.toValue ?: "0",
                currency = model.currency
            )
            TransactionType.TransferNFT -> NftHead(model.nftAsset!!)
            else -> AmountListHead(
                iconUrl = model.assetIcon,
                supportIconUrl = model.assetId.getSupportIconUrl(),
                placeholder = model.assetType.string,
                amount = when (model.type) {
                    TransactionType.StakeDelegate,
                    TransactionType.StakeUndelegate,
                    TransactionType.StakeRewards,
                    TransactionType.StakeRedelegate,
                    TransactionType.StakeWithdraw,
                    TransactionType.Swap,
                    TransactionType.TransferNFT,
                    TransactionType.Transfer -> model.cryptoAmount
                    TransactionType.AssetActivation,
                    TransactionType.TokenApproval -> model.assetSymbol
                    TransactionType.SmartContractCall -> TODO()
                },
                equivalent = when (model.type) {
                    TransactionType.StakeDelegate,
                    TransactionType.StakeUndelegate,
                    TransactionType.StakeRewards,
                    TransactionType.StakeRedelegate,
                    TransactionType.StakeWithdraw,
                    TransactionType.Swap,
                    TransactionType.Transfer -> model.fiatAmount
                    TransactionType.AssetActivation,
                    TransactionType.TransferNFT,
                    TransactionType.TokenApproval -> null
                    TransactionType.SmartContractCall -> TODO()
                },
            )
        }
    }
}

private fun LazyListScope.transactionDateItem(date: String) {
    item {
        PropertyItem(R.string.transaction_date, date)
    }
}

private fun LazyListScope.transactionStatusItem(model: TxDetailsScreenModel) {
    item {
        PropertyItem(
            title = {
                PropertyTitleText(R.string.transaction_status, info = InfoSheetEntity.TransactionInfo(icon = model.assetIcon, state = model.state))
            },
            data = {
                PropertyDataText(
                    text = when (model.state) {
                        TransactionState.Pending -> stringResource(id = R.string.transaction_status_pending)
                        TransactionState.Confirmed -> stringResource(id = R.string.transaction_status_confirmed)
                        TransactionState.Failed -> stringResource(id = R.string.transaction_status_failed)
                        TransactionState.Reverted -> stringResource(id = R.string.transaction_status_reverted)
                    },
                    color = when (model.state) {
                        TransactionState.Pending -> pendingColor
                        TransactionState.Confirmed -> MaterialTheme.colorScheme.tertiary
                        TransactionState.Failed,
                        TransactionState.Reverted -> MaterialTheme.colorScheme.error
                    },
                    badge = {
                        Spacer8()
                        when (model.state) {
                            TransactionState.Pending -> CircularProgressIndicator16(color = pendingColor)
                            else -> null
                        }
                    }
                )
            }
        )
    }
}

private fun LazyListScope.transactionRecipientItem(model: TxDetailsScreenModel) {
    val (title, address) = when (model.type) {
        TransactionType.Swap,
        TransactionType.TokenApproval,
        TransactionType.StakeDelegate,
        TransactionType.StakeUndelegate,
        TransactionType.StakeRewards,
        TransactionType.StakeRedelegate,
        TransactionType.AssetActivation,
        TransactionType.SmartContractCall,
        TransactionType.StakeWithdraw -> return

        TransactionType.Transfer,
        TransactionType.TransferNFT -> when (model.direction) {
            TransactionDirection.SelfTransfer,
            TransactionDirection.Outgoing -> Pair(R.string.transaction_recipient, model.to)
            TransactionDirection.Incoming -> Pair(R.string.transaction_sender, model.from)
        }
    }
    item {
        val context = LocalContext.current
        val clipboardManager = LocalClipboard.current.nativeClipboard

        PropertyItem(
            title = { PropertyTitleText(title) },
            data = {
                Row(
                    modifier = Modifier
                        .clickable { clipboardManager.setPlainText(context, model.to) }
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = address,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.MiddleEllipsis,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer8()
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        tint = MaterialTheme.colorScheme.secondary,
                        contentDescription = ""
                    )
                }
            }
        )
    }
}

private fun LazyListScope.transactionMemoItem(memo: String?) {
    if (memo.isNullOrEmpty()) {
        return
    }
    item {
        PropertyItem(R.string.transfer_memo, memo)
    }
}

private fun LazyListScope.transactionNetworkItem(networkTitle: String, assetId: AssetId) {
    item {
        PropertyItem(
            title = {
                PropertyTitleText(R.string.transfer_network)
            },
            data = {
                PropertyDataText(
                    networkTitle,
                    badge = {
                        Spacer8()
                        AsyncImage(
                            model = assetId.chain.asset(),
                            size = trailingIconMedium,
                            placeholderText = networkTitle,
                        )
                    }
                )
            }
        )
    }
}

private fun LazyListScope.transactionProviderItem(provider: SwapProvider?) {
    provider ?: return
    item {
        PropertyItem(
            title = {
                PropertyTitleText(R.string.swap_provider)
            },
            data = {
                PropertyDataText(
                    provider.name,
                    badge = {
                        Spacer8()
                        AsyncImage(
                            model = provider.getIcon(),
                            size = trailingIconMedium,
                            placeholderText = provider.name
                        )
                    }
                )
            }
        )
    }
}

private fun LazyListScope.transactionNetworkFeeItem(networkTitle: String, feeCrypto: String, feeFiat: String) {
    item {
        PropertyItem(
            modifier = Modifier.height(72.dp),
            title = {
                PropertyTitleText(R.string.transfer_network_fee, info = InfoSheetEntity.NetworkFeeInfo(networkTitle = networkTitle))
            },
            data = {
                Column(horizontalAlignment = Alignment.End) {
                    Row(horizontalArrangement = Arrangement.End) { PropertyDataText(feeCrypto) }
                    Row(horizontalArrangement = Arrangement.End) { PropertyDataText(feeFiat) }
                }
            }
        )
    }
}

private fun LazyListScope.transactionExplorer(explorerName: String, uri: String) {
    item {
        val uriHandler = LocalUriHandler.current
        PropertyItem(
            modifier = Modifier.clickable { uriHandler.open(uri) },
            title = { PropertyTitleText(stringResource(id = R.string.transaction_view_on, explorerName)) },
            data = { PropertyDataText("", badge = { Icon(Icons.Default.ChevronRight, contentDescription = "", tint = MaterialTheme.colorScheme.secondary) }) }
        )
    }
}