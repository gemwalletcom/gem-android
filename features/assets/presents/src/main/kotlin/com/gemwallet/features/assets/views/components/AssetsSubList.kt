package com.gemwallet.features.assets.views.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ui.components.list_item.AssetItemUIModel
import com.gemwallet.android.ui.components.list_item.PinnedAssetsHeaderItem
import com.wallet.core.primitives.AssetId

@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.assets(
    assets: List<AssetItemUIModel>,
    longPressState: MutableState<AssetId?>,
    isPinned: Boolean = false,
    onAssetClick: (AssetId) -> Unit,
    onAssetHide: (AssetId) -> Unit,
    onTogglePin: (AssetId) -> Unit,
) {
    if (assets.isEmpty()) return

    if (isPinned) {
        item { PinnedAssetsHeaderItem() }
    }

    items(items = assets, key = { "${it.asset.id.toIdentifier()}-$isPinned" }) { item ->
        AssetItem(
            modifier = Modifier.testTag(item.asset.id.toIdentifier()),
            item = item,
            longPressState = longPressState,
            isPinned = isPinned,
            onAssetClick = onAssetClick,
            onAssetHide = onAssetHide,
            onTogglePin = onTogglePin,
        )
    }
    if (isPinned) {
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}