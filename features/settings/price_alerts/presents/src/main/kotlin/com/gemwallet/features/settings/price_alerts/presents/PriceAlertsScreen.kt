package com.gemwallet.features.settings.price_alerts.presents

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ui.R
import com.gemwallet.features.settings.price_alerts.viewmodels.PriceAlertViewModel
import com.wallet.core.primitives.AssetId

@Composable
fun PriceAlertsScreen(
    onChart: (AssetId) -> Unit,
    onCancel: () -> Unit,
    viewModel: PriceAlertViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    var selectingAsset by remember { mutableStateOf(false) }
    val alertingAssets by viewModel.alertingAssets.collectAsStateWithLifecycle()
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    val syncState by viewModel.forceSync.collectAsStateWithLifecycle()

//    val toastMessage by remember { stringResource(R.string.price_alerts_enabled_for, "") }

    AnimatedContent(selectingAsset, label = "") { selecting ->
        when (selecting) {
            true -> PriceAlertSelectScreen(
                onCancel = { selectingAsset = false },
                onSelect = {
                    viewModel.addAsset(it) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.price_alerts_enabled_for, it.name),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
                onAdd = { selectingAsset = true }
            )
        }
    }
}