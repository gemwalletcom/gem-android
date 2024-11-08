package com.gemwallet.android.features.settings.price_alerts.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset_select.views.AssetSelectScene
import com.gemwallet.android.features.settings.price_alerts.viewmodels.PriceAlertsAssetSelectViewModel
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype

@Composable
fun AssetSelectScreen(
    onCancel: () -> Unit,
    onSelect: ((AssetId) -> Unit)? = null,
    viewModel: PriceAlertsAssetSelectViewModel = hiltViewModel()
) {
    val uiStates by viewModel.uiState.collectAsStateWithLifecycle()
    val assets by viewModel.assets.collectAsStateWithLifecycle()

    AssetSelectScene(
        title = stringResource(R.string.assets_select_asset),
        titleBadge = { null },
        support = { if (it.asset.id.type() == AssetSubtype.NATIVE) null else it.asset.id.chain.asset().name },
        query = viewModel.queryState,
        assets = assets,
        state = uiStates,
        onSelect = onSelect,
        onCancel = onCancel,
        onAddAsset = null,
        itemTrailing = null,
    )
}