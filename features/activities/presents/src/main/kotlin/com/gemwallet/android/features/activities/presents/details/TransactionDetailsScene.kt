package com.gemwallet.android.features.activities.presents.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.ext.asset
import com.gemwallet.android.features.activities.models.TxDetailsProperty
import com.gemwallet.android.features.activities.models.TxDetailsScreenModel
import com.gemwallet.android.features.activities.presents.details.components.DestinationPropertyItem
import com.gemwallet.android.features.activities.presents.details.components.TxStatusPropertyItem
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_head.AmountListHead
import com.gemwallet.android.ui.components.list_head.NftHead
import com.gemwallet.android.ui.components.list_head.SwapListHead
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyNetworkFee
import com.gemwallet.android.ui.components.list_item.property.PropertyNetworkItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.components.list_item.transaction.getTitle
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.open
import com.wallet.core.primitives.TransactionType

@Composable
fun TransactionDetailsScene(
    model: TxDetailsScreenModel,
    onShare: () -> Unit,
    onFeeDetails: () -> Unit,
    onCancel: () -> Unit,
) {
    Scene(
        title = stringResource(model.type.getTitle(model.direction, model.state)),
        actions = {
            IconButton(onShare) {
                Icon(Icons.Default.Share, "")
            }
        },
        onClose = onCancel,
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            transactionItemHead(model)
            itemsPositioned(model.properties) { position, item ->
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
                item {
                    PropertyNetworkFee(
                        networkTitle = it.name,
                        networkSymbol = it.symbol,
                        feeCrypto = model.feeCrypto,
                        feeFiat = model.feeFiat,
                        variantsAvailable = true,
                        onClick = onFeeDetails,
                    )
                }
            }

            transactionExplorer(model.explorerName, model.explorerUrl)
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
                    TransactionType.PerpetualModifyPosition,
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
                    TransactionType.PerpetualModifyPosition,
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
                    badge = { Icon(Icons.AutoMirrored.Default.ArrowForwardIos, contentDescription = "", tint = MaterialTheme.colorScheme.secondary) }
                )
            },
            listPosition = ListPosition.Single
        )
    }
}