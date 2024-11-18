package com.gemwallet.android.features.asset_select.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset_select.viewmodels.BaseAssetSelectViewModel
import com.gemwallet.android.ui.components.ListItemSupportText
import com.gemwallet.android.ui.models.AssetItemUIModel
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import kotlinx.collections.immutable.toImmutableList

@Composable
fun AssetSelectScreen(
    title: String = "",
    titleBadge: (AssetItemUIModel) -> String?,
    onCancel: () -> Unit,
    onSelect: ((AssetId) -> Unit)? = null,
    predicate: (AssetId) -> Boolean = { true },
    itemTrailing: (@Composable (AssetItemUIModel) -> Unit)? = null,
    itemSupport: ((AssetItemUIModel) -> (@Composable () -> Unit)?)? = null,
    onAddAsset: (() -> Unit)? = null,
    viewModel: BaseAssetSelectViewModel,
) {
    val uiStates by viewModel.uiState.collectAsStateWithLifecycle()
    val assets by viewModel.assets.collectAsStateWithLifecycle()
    val isAddAvailable by viewModel.isAddAssetAvailable.collectAsStateWithLifecycle()

    AssetSelectScene(
        title = title,
        titleBadge = titleBadge,
        support = if (itemSupport == null) {
            {
                if (it.asset.id.type() == AssetSubtype.NATIVE) null else {
                    @Composable { ListItemSupportText(it.asset.id.chain.asset().name) }
                }
            }
        } else {
            itemSupport
        },
        query = viewModel.queryState,
        assets = assets.filter { predicate(it.asset.id) }.toImmutableList(),
        state = uiStates,
        isAddAvailable = isAddAvailable && onAddAsset != null,
        onSelect = onSelect,
        onCancel = onCancel,
        onAddAsset = onAddAsset,
        itemTrailing = itemTrailing,
    )
}