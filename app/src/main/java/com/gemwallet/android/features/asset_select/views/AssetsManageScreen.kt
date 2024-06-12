package com.gemwallet.android.features.asset_select.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset_select.viewmodels.AssetSelectViewModel
import com.gemwallet.android.features.assets.model.AssetUIState
import com.gemwallet.android.ui.components.FatalStateScene
import com.wallet.core.primitives.AssetSubtype

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssetsManageScreen(
    onAddAsset: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AssetSelectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onQuery()
    }

    if (uiState.error.isEmpty()) {
        AssetSelectScene(
            title = stringResource(id = R.string.wallet_manage_token_list),
            titleBadge = ::getAssetBadge,
            support = { if (it.id.type() == AssetSubtype.NATIVE) null else it.id.chain.asset().name },
            query = viewModel.query,
            assets = uiState.assets,
            loading = uiState.isLoading,
            onCancel = onCancel,
            onAddAsset = if (uiState.isAddAssetAvailable) onAddAsset else null,
            onSelect = {},
            actions = {
                if (uiState.isAddAssetAvailable) {
                    IconButton(onClick = onAddAsset) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "")
                    }
                }
            },
            itemTrailing = {asset ->
                Switch(
                    checked = asset.metadata?.isEnabled == true,
                    onCheckedChange = { viewModel.onChangeVisibility(asset.id, it) }
                )
            },
        )
    } else {
        FatalStateScene(
            title = stringResource(id = R.string.wallet_manage_token_list),
            message = uiState.error,
            onCancel = onCancel,
        )
    }
}

internal fun getAssetBadge(asset: AssetUIState): String {
    return if (asset.symbol == asset.name) "" else asset.symbol
}