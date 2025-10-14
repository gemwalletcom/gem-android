package com.gemwallet.features.asset_select.presents.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.type
import com.gemwallet.android.ui.components.list_item.AssetItemUIModel
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.features.asset_select.viewmodels.BaseAssetSelectViewModel
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import kotlinx.collections.immutable.toImmutableList

@Composable
fun AssetSelectScreen(
    title: String = "",
    titleBadge: (AssetItemUIModel) -> String?,
    populartShowed: Boolean = false,
    onCancel: () -> Unit,
    onSelect: ((AssetId) -> Unit)? = null,
    itemTrailing: (@Composable (AssetItemUIModel) -> Unit)? = null,
    itemSupport: ((AssetItemUIModel) -> (@Composable () -> Unit)?)? = null,
    onAddAsset: (() -> Unit)? = null,
    viewModel: BaseAssetSelectViewModel,
) {
    val uiStates by viewModel.uiState.collectAsStateWithLifecycle()
    val popular by viewModel.popular.collectAsStateWithLifecycle()
    val pinned by viewModel.pinned.collectAsStateWithLifecycle()
    val unpinned by viewModel.unpinned.collectAsStateWithLifecycle()
    val isAddAvailable by viewModel.isAddAssetAvailable.collectAsStateWithLifecycle()
    val availableChains by viewModel.availableChains.collectAsStateWithLifecycle()
    val chainsFilter by viewModel.chainFilter.collectAsStateWithLifecycle()
    val balanceFilter by viewModel.balanceFilter.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()

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
        pinned = pinned,
        popular = if (populartShowed) {
            popular
        } else {
            emptyList<AssetItemUIModel>().toImmutableList()
        },
        unpinned = unpinned,
        state = uiStates,
        isAddAvailable = isAddAvailable && onAddAsset != null,
        availableChains = availableChains,
        chainsFilter = chainsFilter,
        balanceFilter = balanceFilter,
        onChainFilter = viewModel::onChainFilter,
        onBalanceFilter = viewModel::onBalanceFilter,
        onClearFilters = viewModel::onClearFilres,
        onSelect = onSelect,
        onCancel = onCancel,
        onAddAsset = onAddAsset,
        itemTrailing = itemTrailing,
        onTagSelect = viewModel::onTagSelect,
        selectedTag = selectedTag,
        tags = viewModel.getTags(),
    )
}