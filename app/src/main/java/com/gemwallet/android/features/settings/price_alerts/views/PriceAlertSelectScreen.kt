package com.gemwallet.android.features.settings.price_alerts.views

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemwallet.android.R
import com.gemwallet.android.features.asset_select.views.AssetSelectScreen
import com.gemwallet.android.features.settings.price_alerts.viewmodels.PriceAlertsSelectViewModel
import com.gemwallet.android.ui.components.PriceInfo
import com.wallet.core.primitives.AssetId

@Composable
fun PriceAlertSelectScreen(
    onCancel: () -> Unit,
    onSelect: ((AssetId) -> Unit)? = null,
    viewModel: PriceAlertsSelectViewModel = hiltViewModel()
) {
    AssetSelectScreen(
        title = stringResource(id = R.string.assets_select_asset),
        titleBadge = { null },
        itemSupport = {
            if (it.price.fiatFormatted.isEmpty()) {
                null
            } else {
                @Composable {
                    PriceInfo(
                        price = it.price,
                        style = MaterialTheme.typography.bodyMedium,
                        internalPadding = 4.dp
                    )
                }
            }
        },
        onCancel = onCancel,
        onSelect = onSelect,
        viewModel = viewModel,
    )
}