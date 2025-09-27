package com.gemwallet.features.asset.presents.details.views.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText

internal fun LazyListScope.additionBalance(
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