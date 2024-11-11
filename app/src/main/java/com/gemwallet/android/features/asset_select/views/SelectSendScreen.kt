package com.gemwallet.android.features.asset_select.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemwallet.android.R
import com.gemwallet.android.features.asset_select.viewmodels.SendSelectViewModel
import com.gemwallet.android.ui.components.getBalanceInfo
import com.wallet.core.primitives.AssetId

@Composable
fun SelectSendScreen(
    onCancel: () -> Unit,
    onSelect: ((AssetId) -> Unit)?,
    viewModel: SendSelectViewModel = hiltViewModel()
) {
    AssetSelectScreen(
        title = stringResource(id = R.string.wallet_send),
        titleBadge = { null },
        itemTrailing = { getBalanceInfo(it)() },
        onSelect = onSelect,
        onCancel = onCancel,
        viewModel = viewModel,
    )
}