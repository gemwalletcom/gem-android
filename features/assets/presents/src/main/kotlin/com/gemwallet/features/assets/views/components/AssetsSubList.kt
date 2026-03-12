package com.gemwallet.features.assets.views.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gemwallet.android.domains.asset.aggregates.AssetInfoDataAggregate
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ui.components.list_item.PinnedAssetsHeaderItem
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.models.AssetsGroupType
import com.wallet.core.primitives.AssetId

@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.assets(
    items: List<AssetInfoDataAggregate>,
    longPressState: MutableState<AssetId?>,
    group: AssetsGroupType,
    onAssetClick: (AssetId) -> Unit,
    onAssetHide: (AssetId) -> Unit,
    onTogglePin: (AssetId) -> Unit,
) {
    if (items.isEmpty()) return

    item { PinnedAssetsHeaderItem(group) }

    itemsPositioned(items = items, key = { _, item -> "${item.id.toIdentifier()}-${group.name}" }) { position, item ->
        AssetItem(
            modifier = Modifier.testTag(item.id.toIdentifier()),
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