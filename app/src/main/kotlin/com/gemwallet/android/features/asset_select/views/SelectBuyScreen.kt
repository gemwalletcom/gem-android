package com.gemwallet.android.features.asset_select.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemwallet.android.features.asset_select.viewmodels.BuySelectViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.getBalanceInfo
import com.gemwallet.android.ui.models.actions.CancelAction
import com.wallet.core.primitives.AssetId

@Composable
fun SelectBuyScreen(
    cancelAction: CancelAction,
    onSelect: ((AssetId) -> Unit)?,
    viewModel: BuySelectViewModel = hiltViewModel()
) {
    AssetSelectScreen(
        title = stringResource(id = R.string.wallet_buy),
        titleBadge = { null },
        itemTrailing = { getBalanceInfo(it)() },
        onSelect = onSelect,
        onCancel = { cancelAction.invoke() },
        viewModel = viewModel,
    )
}