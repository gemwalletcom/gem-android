package com.gemwallet.features.asset.presents.details.components

import androidx.compose.foundation.lazy.LazyListScope
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.features.asset.viewmodels.details.models.AssetInfoUIModel

internal fun LazyListScope.balancesHeader(model: AssetInfoUIModel.AccountInfoUIModel) {
    if (model.available.isEmpty() && model.stake.isEmpty() && model.reserved.isEmpty()) {
        return
    }
    item {
        SubheaderItem(R.string.asset_balances)
    }
}