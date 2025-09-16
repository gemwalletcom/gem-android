package com.gemwallet.features.settings.price_alerts.presents

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gemwallet.features.settings.price_alerts.viewmodels.PriceAlertsSelectViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.PriceInfo
import com.gemwallet.features.asset_select.presents.views.AssetSelectScreen
import com.gemwallet.features.asset_select.presents.views.getAssetBadge
import com.wallet.core.primitives.AssetId

@Composable
fun PriceAlertSelectScreen(
    onCancel: () -> Unit,
    onSelect: ((AssetId) -> Unit)? = null,
    viewModel: PriceAlertsSelectViewModel = hiltViewModel()
) {
    AssetSelectScreen(
        title = stringResource(id = R.string.assets_select_asset),
        titleBadge = ::getAssetBadge,
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