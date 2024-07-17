package com.gemwallet.android.features.asset_select.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset_select.viewmodels.AssetSelectViewModel
import com.gemwallet.android.features.assets.model.AssetUIState
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import kotlinx.collections.immutable.toImmutableList

@Composable
fun AssetSelectScreen(
    title: String = "",
    titleBadge: (AssetUIState) -> String?,
    onCancel: () -> Unit,
    onSelect: ((AssetId) -> Unit)? = null,
    predicate: (AssetId) -> Boolean = { true },
    itemTrailing: (@Composable (AssetUIState) -> Unit)? = null,
    onAddAsset: (() -> Unit)? = null,
    viewModel: AssetSelectViewModel = hiltViewModel()
) {
    val uiStates by viewModel.isLoading.collectAsStateWithLifecycle()
    val assets by viewModel.assets.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onQuery()
    }

    AssetSelectScene(
        title = title,
        titleBadge = titleBadge,
        support = { if (it.asset.id.type() == AssetSubtype.NATIVE) null else it.asset.id.chain.asset().name },
        query = viewModel.queryState,
        assets = assets.filter { predicate(it.asset.id) }.toImmutableList(), // TODO: Empty balance???
        loading = uiStates,
        onSelect = onSelect,
        onCancel = onCancel,
        onAddAsset = onAddAsset,
        itemTrailing = itemTrailing,
    )
}