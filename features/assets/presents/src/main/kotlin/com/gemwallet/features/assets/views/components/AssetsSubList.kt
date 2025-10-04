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
import com.gemwallet.android.ui.models.ListPosition
import com.wallet.core.primitives.AssetId

@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.assets(
    items: List<AssetItemUIModel>,
    longPressState: MutableState<AssetId?>,
    isPinned: Boolean = false,
    onAssetClick: (AssetId) -> Unit,
    onAssetHide: (AssetId) -> Unit,
    onTogglePin: (AssetId) -> Unit,
) {
    if (items.isEmpty()) return

    if (isPinned) {
        item { PinnedAssetsHeaderItem() }
    }
    val size = items.size
    itemsIndexed(items = items, key = { index, item -> "${item.asset.id.toIdentifier()}-$isPinned" }) { index, item ->
        AssetItem(
            modifier = Modifier.testTag(item.asset.id.toIdentifier()),
            listPosition = ListPosition.getPosition(index, size),
            item = item,
            longPressState = longPressState,
            isPinned = isPinned,
            onAssetClick = onAssetClick,
            onAssetHide = onAssetHide,
            onTogglePin = onTogglePin,
        )
    }
}