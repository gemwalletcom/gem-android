package com.gemwallet.android.features.settings.price_alerts.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.features.asset_select.views.AssetSelectScreen
import com.gemwallet.android.features.settings.price_alerts.viewmodels.PriceAlertViewModel
import com.wallet.core.primitives.AssetId

@Composable
fun PriceAlertScreen(
    onChart: (AssetId) -> Unit,
    onCancel: () -> Unit,
    viewModel: PriceAlertViewModel = hiltViewModel(),
) {
    var selectingAsset by remember { mutableStateOf(false) }
    val alertingAssets by viewModel.alertingAssets.collectAsStateWithLifecycle()

    AnimatedContent(selectingAsset, label = "") { selecting ->
        when (selecting) {
            true -> AssetSelectScreen(
                title = stringResource(R.string.assets_select_asset),
                titleBadge = { null },
                onCancel = { selectingAsset = false },
                onSelect = viewModel::addAsset
            )
            false -> PriceAlertScene(
                alertingPrice = alertingAssets,
                onChart = onChart,
                onExclude = viewModel::excludeAsset,
                onCancel = onCancel,
                onAdd = {  selectingAsset = true }
            )
        }
    }
}