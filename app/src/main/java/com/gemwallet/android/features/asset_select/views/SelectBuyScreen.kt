package com.gemwallet.android.features.asset_select.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemwallet.android.R
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.asset_select.components.ItemBalanceTrailing
import com.gemwallet.android.features.asset_select.viewmodels.SelectBuyAssetViewModel
import com.wallet.core.primitives.AssetId

@Composable
fun SelectBuyScreen(
    onCancel: () -> Unit,
    onSelect: ((AssetId) -> Unit)?,
) {
    val viewModel: SelectBuyAssetViewModel = hiltViewModel()
    AssetSelectScreen(
        title = stringResource(id = R.string.wallet_buy),
        titleBadge = { null },
        itemTrailing = { ItemBalanceTrailing(it) },
        predicate = { viewModel.getAvailableToBuy().contains(it.toIdentifier()) },
        onSelect = onSelect,
        onCancel = onCancel,
    )
}