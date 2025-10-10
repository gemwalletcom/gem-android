package com.gemwallet.android.features.activities.presents.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.ext.asset
import com.gemwallet.android.features.activities.models.TxDetailsProperty
import com.gemwallet.android.features.activities.presents.details.components.DestinationPropertyItem
import com.gemwallet.android.features.activities.presents.details.components.TxStatusPropertyItem
import com.gemwallet.android.features.activities.viewmodels.TransactionDetailsViewModel
import com.gemwallet.android.features.activities.viewmodels.TxDetailsScreenModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_head.AmountListHead
import com.gemwallet.android.ui.components.list_head.NftHead
import com.gemwallet.android.ui.components.list_head.SwapListHead
import com.gemwallet.android.ui.components.list_item.getTransactionTitle
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyNetworkFee
import com.gemwallet.android.ui.components.list_item.property.PropertyNetworkItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.screen.LoadingScene
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.models.getListPosition
import com.gemwallet.android.ui.open
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
                itemsIndexed(model.properties) { index, item ->
                    val position = model.properties.getListPosition(index)
                    when (item) {
                        is TxDetailsProperty.Date -> PropertyItem(R.string.transaction_date, item.data, listPosition = position)
                        is TxDetailsProperty.Destination -> DestinationPropertyItem(item, position)
                        is TxDetailsProperty.Memo -> PropertyItem(R.string.transfer_memo, item.data, listPosition = position)
                        is TxDetailsProperty.Network -> PropertyNetworkItem(item.data.chain, listPosition = position)
                        is TxDetailsProperty.Provider -> PropertyItem(R.string.common_provider, item.data.name, listPosition = position)
                        is TxDetailsProperty.Status -> TxStatusPropertyItem(item, position)
                    }
                }
                model.asset.chain.asset().let {
                    item { PropertyNetworkFee(it.name, it.symbol, model.feeCrypto, model.feeFiat) }
                }

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
                icon = model.asset,
                amount = when (model.type) {
                    TransactionType.StakeDelegate,
                    TransactionType.StakeUndelegate,
                    TransactionType.StakeRewards,
                    TransactionType.StakeRedelegate,
                    TransactionType.StakeWithdraw,
                    TransactionType.Swap,
                    TransactionType.TransferNFT,
                    TransactionType.StakeFreeze,
                    TransactionType.StakeUnfreeze,
                    TransactionType.Transfer -> model.cryptoAmount
                    TransactionType.AssetActivation,
                    TransactionType.SmartContractCall,
                    TransactionType.PerpetualOpenPosition,
                    TransactionType.PerpetualClosePosition,
                    TransactionType.TokenApproval -> model.asset.symbol
                },
                equivalent = when (model.type) {
                    TransactionType.StakeDelegate,
                    TransactionType.StakeUndelegate,
                    TransactionType.StakeRewards,
                    TransactionType.StakeRedelegate,
                    TransactionType.StakeWithdraw,
                    TransactionType.Swap,
                    TransactionType.StakeFreeze,
                    TransactionType.StakeUnfreeze,
                    TransactionType.Transfer -> model.fiatAmount
                    TransactionType.AssetActivation,
                    TransactionType.TransferNFT,
                    TransactionType.SmartContractCall,
                    TransactionType.PerpetualOpenPosition,
                    TransactionType.PerpetualClosePosition,
                    TransactionType.TokenApproval -> null
                },
            )
        }
    }
}

private fun LazyListScope.transactionExplorer(explorerName: String, uri: String) {
    item {
        val uriHandler = LocalUriHandler.current
        val context = LocalContext.current
        PropertyItem(
            modifier = Modifier.clickable { uriHandler.open(context, uri) },
            title = { PropertyTitleText(stringResource(id = R.string.transaction_view_on, explorerName)) },
            data = {
                PropertyDataText(
                    text = "",
                    badge = { Icon(Icons.Default.ChevronRight, contentDescription = "", tint = MaterialTheme.colorScheme.secondary) }
                )
            },
            listPosition = ListPosition.Single
        )
    }
}