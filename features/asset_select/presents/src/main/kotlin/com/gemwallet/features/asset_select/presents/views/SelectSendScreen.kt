package com.gemwallet.features.asset_select.presents.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.getBalanceInfo
import com.gemwallet.features.asset_select.viewmodels.SendSelectViewModel
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