package com.gemwallet.android.features.settings.price_alerts.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.settings.price_alerts.viewmodels.PriceAlertViewModel
import com.wallet.core.primitives.AssetId

@Composable
fun PriceAlertsScreen(
    onChart: (AssetId) -> Unit,
    onCancel: () -> Unit,
    viewModel: PriceAlertViewModel = hiltViewModel(),
) {
    var selectingAsset by remember { mutableStateOf(false) }
    val alertingAssets by viewModel.alertingAssets.collectAsStateWithLifecycle()
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    val syncState by viewModel.forceSync.collectAsStateWithLifecycle()

    AnimatedContent(selectingAsset, label = "") { selecting ->
        when (selecting) {
            true -> PriceAlertSelectScreen(
                onCancel = { selectingAsset = false },
                onSelect = {
                    viewModel.addAsset(it)
                    selectingAsset = false
                },
            )
            false -> PriceAlertScene(
                alertingPrice = alertingAssets,
                enabled = enabled,
                syncState = syncState,
                onEnablePriceAlerts = viewModel::onEnablePriceAlerts,
                onChart = onChart,
                onExclude = viewModel::excludeAsset,
                onRefresh = viewModel::refresh,
                onCancel = onCancel,
                onAdd = {  selectingAsset = true }
            )
        }
    }
}