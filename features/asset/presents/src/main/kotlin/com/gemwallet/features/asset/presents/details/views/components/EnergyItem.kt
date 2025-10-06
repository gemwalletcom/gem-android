package com.gemwallet.features.asset.presents.details.views.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.models.ListPosition
import com.wallet.core.primitives.BalanceMetadata

fun LazyListScope.energyItem(balanceMetadata: BalanceMetadata?) {
    balanceMetadata?.let { metadata ->
        item {
            SubheaderItem(title = stringResource(id = R.string.asset_balances))
            PropertyItem(title = R.string.stake_resource_energy, "${metadata.energyAvailable} / ${metadata.energyTotal}", listPosition = ListPosition.First)
            PropertyItem(title = R.string.stake_resource_bandwidth, "${metadata.bandwidthAvailable} / ${metadata.bandwidthTotal}", listPosition = ListPosition.Last)

        }
    }
}