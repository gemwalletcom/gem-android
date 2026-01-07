package com.gemwallet.android.features.activities.presents.details

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.domains.transaction.aggregates.TransactionDetailsAggregate
import com.gemwallet.android.domains.transaction.values.TransactionDetailsValue
import com.gemwallet.android.features.activities.presents.details.components.DestinationPropertyItem
import com.gemwallet.android.features.activities.presents.details.components.TransactionExplorer
import com.gemwallet.android.features.activities.presents.details.components.TransactionStatusProperty
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_head.AmountListHead
import com.gemwallet.android.ui.components.list_head.NftHead
import com.gemwallet.android.ui.components.list_head.SwapListHead
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyNetworkFee
import com.gemwallet.android.ui.components.list_item.property.PropertyNetworkItem
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.components.list_item.transaction.getTitle
import com.gemwallet.android.ui.components.screen.Scene

@Composable
fun TransactionDetailsScene(
    data: TransactionDetailsAggregate,
    onShare: () -> Unit,
    onFeeDetails: () -> Unit,
    onCancel: () -> Unit,
) {
    Scene(
        title = data.getTitle(),
        actions = {
            IconButton(onShare) {
                Icon(Icons.Default.Share, "")
            }
        },
        onClose = onCancel,
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            data.valueGroups.forEach { group ->
                itemsPositioned(group.items) { position, item ->
                    when (item) {
                        is TransactionDetailsValue.Amount.NFT -> NftHead(item.asset)
                        TransactionDetailsValue.Amount.None -> {}
                        is TransactionDetailsValue.Amount.Plain -> AmountListHead(
                            icon = item.asset,
                            amount = item.value,
                            equivalent = item.equivalent,
                        )
                        is TransactionDetailsValue.Amount.Swap -> SwapListHead(
                            fromAsset = item.fromAsset,
                            fromValue = item.fromValue,
                            toAsset = item.toAsset,
                            toValue = item.toValue,
                            currency = item.currency
                        )
                        is TransactionDetailsValue.Date -> PropertyItem(R.string.transaction_date, item.data, listPosition = position)
                        is TransactionDetailsValue.Destination -> DestinationPropertyItem(item, position)
                        is TransactionDetailsValue.Explorer -> TransactionExplorer(
                            item.name,
                            item.url
                        )
                        is TransactionDetailsValue.Fee -> PropertyNetworkFee(
                            networkTitle = item.asset.name,
                            networkSymbol = item.asset.symbol,
                            feeCrypto = item.value,
                            feeFiat = item.equivalent,
                            variantsAvailable = true,
                            onClick = onFeeDetails,
                        )
                        is TransactionDetailsValue.Memo -> PropertyItem(R.string.transfer_memo, item.data, listPosition = position)
                        is TransactionDetailsValue.Network -> PropertyNetworkItem(item.data.chain, listPosition = position)
                        is TransactionDetailsValue.Status -> TransactionStatusProperty(data.asset, item, position)
                    }
                }
            }
        }
    }
}