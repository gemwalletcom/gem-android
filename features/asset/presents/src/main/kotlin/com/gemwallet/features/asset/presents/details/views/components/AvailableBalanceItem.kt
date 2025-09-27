package com.gemwallet.features.asset.presents.details.views.components

import androidx.compose.foundation.lazy.LazyListScope
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.property.PropertyItem

internal fun LazyListScope.availableBalance(balance: String?) {
    if (balance.isNullOrEmpty()) {
        return
    }
    item {
        PropertyItem(R.string.asset_balances_available, balance)
    }
}