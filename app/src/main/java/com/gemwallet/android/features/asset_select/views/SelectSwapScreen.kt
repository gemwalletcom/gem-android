package com.gemwallet.android.features.asset_select.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.features.asset_select.viewmodels.SwapSelectViewModel
import com.gemwallet.android.features.swap.models.SwapPairSelect
import com.gemwallet.android.ui.components.getBalanceInfo

@Composable
fun SelectSwapScreen(
    select: SwapPairSelect,
    onCancel: () -> Unit,
    onSelect: ((SwapPairSelect) -> Unit)?,
    viewModel: SwapSelectViewModel = hiltViewModel()
) {
    LaunchedEffect(select.fromId, select.toId) {
        viewModel.setPair(select)
    }

    val uiStates by viewModel.uiState.collectAsStateWithLifecycle()
    val assets by viewModel.assets.collectAsStateWithLifecycle()

    AssetSelectScene(
        title = when (select) {
            is SwapPairSelect.From -> stringResource(id = R.string.swap_you_pay)
            is SwapPairSelect.To -> stringResource(id = R.string.swap_you_receive)
        },
        titleBadge = { null },
        query = viewModel.queryState,
        assets = assets,
        state = uiStates,
        onSelect = { onSelect?.invoke(select.select(it)) },
        onCancel = onCancel,
        onAddAsset = null,
        itemTrailing =  { getBalanceInfo(it)() },
        support = null,
    )
}