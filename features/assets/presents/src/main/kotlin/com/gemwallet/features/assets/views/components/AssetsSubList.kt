package com.gemwallet.features.assets.views.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ui.components.list_item.AssetItemUIModel
import com.gemwallet.android.ui.components.list_item.PinnedAssetsHeaderItem
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.models.AssetsGroupType
import com.gemwallet.android.ui.models.ListPosition
import com.wallet.core.primitives.AssetId

@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.assets(
    items: List<AssetItemUIModel>,
    longPressState: MutableState<AssetId?>,
    group: AssetsGroupType,
    onAssetClick: (AssetId) -> Unit,
    onAssetHide: (AssetId) -> Unit,
    onTogglePin: (AssetId) -> Unit,
) {
    if (items.isEmpty()) return

    item { PinnedAssetsHeaderItem(group) }

    itemsPositioned(items = items, key = { index, item -> "${item.asset.id.toIdentifier()}-${group.name}" }) { position, item ->
        AssetItem(
            modifier = Modifier,//.testTag(item.asset.id.toIdentifier()),
            listPosition = position,
            item = item,
            longPressState = longPressState,
            group = group,
            onAssetClick = onAssetClick,
            onAssetHide = onAssetHide,
            onTogglePin = onTogglePin,
        )
    }
}