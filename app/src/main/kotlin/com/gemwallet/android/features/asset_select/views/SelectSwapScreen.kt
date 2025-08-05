package com.gemwallet.android.features.asset_select.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.type
import com.gemwallet.android.features.asset_select.viewmodels.SwapSelectViewModel
import com.gemwallet.android.features.swap.viewmodels.models.SwapPairSelect
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.getBalanceInfo
import com.wallet.core.primitives.AssetSubtype
import kotlinx.coroutines.coroutineScope

@Composable
fun SelectSwapScreen(
    select: SwapPairSelect,
    onCancel: () -> Unit,
    onSelect: ((SwapPairSelect) -> Unit)?,
    viewModel: SwapSelectViewModel = hiltViewModel()
) {
    val uiStates by viewModel.uiState.collectAsStateWithLifecycle()
    val pinned by viewModel.pinned.collectAsStateWithLifecycle()
    val unpinned by viewModel.unpinned.collectAsStateWithLifecycle()
    val availableChains by viewModel.availableChains.collectAsStateWithLifecycle()
    val chainsFilter by viewModel.chainFilter.collectAsStateWithLifecycle()
    val balanceFilter by viewModel.balanceFilter.collectAsStateWithLifecycle()

    LaunchedEffect(select.fromId, select.toId) {
        coroutineScope {
            viewModel.setPair(select)
        }
    }

    AssetSelectScene(
        title = when (select) {
            is SwapPairSelect.From -> stringResource(id = R.string.swap_you_pay)
            is SwapPairSelect.To -> stringResource(id = R.string.swap_you_receive)
        },
        titleBadge = { null },
        query = viewModel.queryState,
        pinned = pinned,
        unpinned = unpinned,
        state = uiStates,
        availableChains = availableChains,
        chainsFilter = chainsFilter,
        balanceFilter = balanceFilter,
        onChainFilter = viewModel::onChainFilter,
        onBalanceFilter = viewModel::onBalanceFilter,
        onClearFilters = viewModel::onClearFilres,
        onSelect = { onSelect?.invoke(select.select(it)) },
        onCancel = onCancel,
        onAddAsset = null,
        itemTrailing =  { getBalanceInfo(it)() },
        support = {
            if (it.asset.id.type() == AssetSubtype.NATIVE) null else {
                @Composable { ListItemSupportText(it.asset.id.chain.asset().name) }
            }
        },
    )
}