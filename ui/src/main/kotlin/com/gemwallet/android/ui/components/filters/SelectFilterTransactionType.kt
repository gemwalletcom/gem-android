package com.gemwallet.android.ui.components.filters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.models.TransactionTypeFilter

fun LazyListScope.selectFilterTransactionType(
    filter: List<TransactionTypeFilter>,
    onFilter: (TransactionTypeFilter) -> Unit,
) {
    item {
        SubheaderItem(stringResource(R.string.filter_types))
    }
    itemsPositioned(TransactionTypeFilter.entries) { position, item ->
        PropertyItem(
            modifier = Modifier.clickable { onFilter(item) },
            title = { PropertyTitleText(item.getLabel()) },
            data = {
                if (filter.contains(item)) {
                    Icon(Icons.Default.CheckCircleOutline, contentDescription = "")
                }
            },
            listPosition = position,
        )
    }
}

fun TransactionTypeFilter.getLabel() = when (this) {
    TransactionTypeFilter.Transfer -> R.string.transfer_title
    TransactionTypeFilter.Swap -> R.string.wallet_swap
    TransactionTypeFilter.Stake -> R.string.wallet_stake
    TransactionTypeFilter.SmartContract -> R.string.transfer_smart_contract_title
    TransactionTypeFilter.Other -> R.string.transfer_other_title
}