package com.gemwallet.features.asset.presents.details.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.models.ListPosition

fun LazyListScope.manageAssetItem(
    assetInfo: AssetInfo,
    onPin: () -> Unit,
    onAdd: () -> Unit,
) {
    if (assetInfo.owner != null) {
        return
    }

    item {
        PropertyItem(
            modifier = Modifier.clickable(onClick = onPin),
            title = { PropertyTitleText(R.string.common_pin, trailing = { Icon(Icons.Default.PushPin, stringResource(R.string.common_pin)) }) },
            data = {
                PropertyDataText(
                    text = "",
                    badge = {
                        DataBadgeChevron()
                    }
                )
            },
            listPosition = ListPosition.First,
        )
        PropertyItem(
            modifier = Modifier.clickable(onClick = onAdd),
            title = { PropertyTitleText(R.string.asset_add_to_wallet, trailing = { Icon(Icons.Default.AddCircleOutline, "") }) },
            data = {
                PropertyDataText(
                    text = "",
                    badge = {
                        DataBadgeChevron()
                    }
                )
            },
            listPosition = ListPosition.Last,
        )
    }
}