package com.gemwallet.android.features.asset_select.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.assetType
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset_select.components.SearchBar
import com.gemwallet.android.features.asset_select.viewmodels.AssetSelectViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.ChainItem
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.image.getIconUrl
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.gemwallet.android.ui.models.AssetItemUIModel
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain

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

    val selectedChains by viewModel.chainFilter.collectAsStateWithLifecycle()

    var showSelectNetworks by remember { mutableStateOf(false) }

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
        onCancel = onCancel,
        onAddAsset = if (isAddAssetAvailable) onAddAsset else null,
        onSelect = {},
        actions = {
            IconButton(onClick = { showSelectNetworks = !showSelectNetworks }) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    tint = if (selectedChains.isEmpty()) LocalContentColor.current else MaterialTheme.colorScheme.primary,
                    contentDescription = "Filter by networks",
                )
            }
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

    if (showSelectNetworks) {
        SelectNetwork(selectedChains, { showSelectNetworks = false }, viewModel::onChainFilter)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SelectNetwork(
    selected: List<Chain>,
    onDismissRequest: () -> Unit,
    onSelect: (Chain) -> Unit,
) {
    var chainFilter = rememberTextFieldState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchBar(chainFilter, Modifier.padding(horizontal = padding16))
            Spacer16()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                Chain.entries
                    .map { it.asset() }
                    .filter { asset ->
                        val query = chainFilter.text.toString().lowercase()
                        asset.name.lowercase().contains(query)
                                || asset.symbol.lowercase().contains(query)
                                || (asset.id.chain.assetType()?.string?.lowercase()?.contains(query) == true)
                    }
                    .forEach {
                        val chain = it.id.chain
                        item {
                            ChainItem(
                                chain = chain,
                                title = chain.asset().name,
                                icon = chain.getIconUrl(),
                                trailing = {
                                    if (selected.contains(chain)) {
                                        Icon(Icons.Default.CheckCircleOutline, contentDescription = "")
                                    }
                                }
                            ) { onSelect(chain) }
                        }
                    }
            }
        }
    }
}

fun getAssetBadge(item: AssetItemUIModel): String {
    return if (item.asset.symbol == item.asset.name) "" else item.asset.symbol
}