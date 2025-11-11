package com.gemwallet.features.asset_select.presents.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.type
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.SearchBar
import com.gemwallet.android.ui.components.list_item.AssetItemUIModel
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.getBalanceInfo
import com.gemwallet.android.ui.components.list_item.listItem
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.features.asset_select.viewmodels.AssetSelectViewModel
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import kotlinx.collections.immutable.toImmutableList

@Composable
fun AssetsManageScreen(
    manageable: Boolean = false,
    onAddAsset: () -> Unit,
    onAssetClick: (AssetId) -> Unit,
    onCancel: () -> Unit,
    viewModel: AssetSelectViewModel = hiltViewModel()
) {
    val isAddAssetAvailable by viewModel.isAddAssetAvailable.collectAsStateWithLifecycle()
    val uiStates by viewModel.uiState.collectAsStateWithLifecycle()
    val pinned by viewModel.pinned.collectAsStateWithLifecycle()
    val unpinned by viewModel.unpinned.collectAsStateWithLifecycle()

    val availableChains by viewModel.availableChains.collectAsStateWithLifecycle()
    val chainsFilter by viewModel.chainFilter.collectAsStateWithLifecycle()
    val balanceFilter by viewModel.balanceFilter.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()

    AssetSelectScene(
        title = {
            if (manageable) {
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.wallet_manage_token_list),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                SearchBar(
                    query = viewModel.queryState,
                    modifier = Modifier.listItem(ListPosition.Single, paddingHorizontal = 0.dp)
                )
            }
        },
        titleBadge = ::getAssetBadge,
        support = {
            if (it.asset.id.type() == AssetSubtype.NATIVE) null else {
                { ListItemSupportText(it.asset.id.chain.asset().name) }
            }
        },
        query = viewModel.queryState,
        selectedTag = selectedTag,
        tags = viewModel.getTags(),
        pinned = pinned,
        popular = emptyList<AssetItemUIModel>().toImmutableList(),
        unpinned = unpinned,
        state = uiStates,
        isAddAvailable = isAddAssetAvailable,
        availableChains = availableChains,
        chainsFilter = chainsFilter,
        balanceFilter = balanceFilter,
        searchable = manageable,
        onChainFilter = viewModel::onChainFilter,
        onBalanceFilter = viewModel::onBalanceFilter,
        onClearFilters = viewModel::onClearFilres,
        onCancel = onCancel,
        onAddAsset = if (isAddAssetAvailable) onAddAsset else null,
        onSelect = onAssetClick,
        actions = {
            if (isAddAssetAvailable && manageable) {
                IconButton(onClick = onAddAsset) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "")
                }
            }
        },
        onTagSelect = viewModel::onTagSelect,
        itemTrailing = { asset ->
            if (manageable) {
                Switch(
                    checked = asset.metadata?.isEnabled == true,
                    onCheckedChange = { viewModel.onChangeVisibility(asset.asset.id, it) }
                )
            } else {
                getBalanceInfo(asset)()
            }
        },
    )
}

fun getAssetBadge(item: AssetItemUIModel): String {
    return if (item.asset.symbol == item.asset.name) "" else item.asset.symbol
}