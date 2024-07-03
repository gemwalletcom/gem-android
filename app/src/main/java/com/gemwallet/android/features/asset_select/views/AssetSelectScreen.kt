package com.gemwallet.android.features.asset_select.views

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset_select.viewmodels.AssetSelectViewModel
import com.gemwallet.android.features.assets.model.AssetUIState
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.ui.components.FatalStateScene
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype

@Composable
fun AssetSelectScreen(
    title: String = "",
    titleBadge: (AssetUIState) -> String?,
    onCancel: () -> Unit,
    onSelect: ((AssetId) -> Unit)? = null,
    predicate: (AssetInfo) -> Boolean = { true },
    itemTrailing: (@Composable (AssetUIState) -> Unit)? = null,
    onAddAsset: (() -> Unit)? = null,
    viewModel: AssetSelectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(key1 = predicate) {
        viewModel.setPredicate(predicate)
        viewModel.query.clearText()

        onDispose {  }
    }

    LaunchedEffect(Unit) {
        viewModel.onQuery()
    }


    if (uiState.error.isEmpty()) {
        AssetSelectScene(
            title = title,
            titleBadge = titleBadge,
            support = { if (it.id.type() == AssetSubtype.NATIVE) null else it.id.chain.asset().name },
            query = viewModel.query,
            assets = uiState.assets,
            loading = uiState.isLoading,
            onSelect = onSelect,
            onCancel = onCancel,
            onAddAsset = onAddAsset,
            itemTrailing = itemTrailing,
        )
    } else {
        FatalStateScene(
            title = title,
            message = uiState.error,
            onCancel = onCancel,
        )
    }
}