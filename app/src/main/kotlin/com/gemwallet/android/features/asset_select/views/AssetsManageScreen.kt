package com.gemwallet.android.features.asset_select.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset_select.viewmodels.AssetSelectViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.AssetItemUIModel
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.wallet.core.primitives.AssetSubtype

@Composable
fun AssetsManageScreen(
    onAddAsset: () -> Unit,
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

    AssetSelectScene(
        title = stringResource(id = R.string.wallet_manage_token_list),
        titleBadge = ::getAssetBadge,
        support = {
            if (it.asset.id.type() == AssetSubtype.NATIVE) null else {
                { ListItemSupportText(it.asset.id.chain.asset().name) }
            }
        },
        query = viewModel.queryState,
        pinned = pinned,
        unpinned = unpinned,
        state = uiStates,
        isAddAvailable = isAddAssetAvailable,
        availableChains = availableChains,
        chainsFilter = chainsFilter,
        balanceFilter = balanceFilter,
        onChainFilter = viewModel::onChainFilter,
        onBalanceFilter = viewModel::onBalanceFilter,
        onClearFilters = viewModel::onClearFilres,
        onCancel = onCancel,
        onAddAsset = if (isAddAssetAvailable) onAddAsset else null,
        onSelect = {},
        actions = {
            if (isAddAssetAvailable) {
                IconButton(onClick = onAddAsset) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "")
                }
            }
        },
        itemTrailing = {asset ->
            Switch(
                checked = asset.metadata?.isEnabled == true,
                onCheckedChange = { viewModel.onChangeVisibility(asset.asset.id, it) }
            )
        },
    )
}

fun getAssetBadge(item: AssetItemUIModel): String {
    return if (item.asset.symbol == item.asset.name) "" else item.asset.symbol
}