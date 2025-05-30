package com.gemwallet.android.features.asset_select.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.assetType
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset_select.components.SearchBar
import com.gemwallet.android.features.asset_select.viewmodels.AssetSelectViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
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
            SelectNetwork(selectedChains, viewModel::onChainFilter)
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SelectNetwork(
    selected: List<Chain>,
    onSelect: (Chain) -> Unit,
) {
    var checked by remember { mutableStateOf(false) }
    var yOffset by remember { mutableStateOf(0.dp) }
    var chainFilter = rememberTextFieldState()

    val density = LocalDensity.current

    Box(modifier = Modifier.wrapContentSize()) {
        IconButton(
            modifier = Modifier.onSizeChanged({
                yOffset = with(density) { it.height.toDp() }
            }),
            onClick = {
                checked = !checked
                chainFilter.clearText()
            }
        ) {
            Icon(
                imageVector = Icons.Default.FilterAlt,
                tint = if (selected.isEmpty()) LocalContentColor.current else MaterialTheme.colorScheme.primary,
                contentDescription = "Filter by networks",
            )
        }
        DropdownMenu(
            modifier = Modifier.fillMaxHeight(0.5f).widthIn(min = 250.dp),
            expanded = checked,
            onDismissRequest = { checked = false },
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            DropdownMenuItem(
                text = { SearchBar(chainFilter) },
                onClick = { /* Handle edit! */ },
            )
            Chain.entries
                .map { it.asset() }
                .filter { asset ->
                    val query = chainFilter.text.toString().lowercase()
                    asset.name.lowercase().contains(query)
                            || asset.symbol.lowercase().contains(query)
                            || (asset.id.chain.assetType()?.string?.lowercase()?.contains(query) ?: false)
                }
                .forEach {
                    val chain = it.id.chain
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = it.name
                                )
                                if (selected.contains(chain)) {
                                    Icon(Icons.Default.CheckCircleOutline, contentDescription = "")
                                }
                            }
                        },
                        onClick = { onSelect(chain) },
                        leadingIcon = { AsyncImage(it) }
                    )
                }
        }
    }
}

fun getAssetBadge(item: AssetItemUIModel): String {
    return if (item.asset.symbol == item.asset.name) "" else item.asset.symbol
}