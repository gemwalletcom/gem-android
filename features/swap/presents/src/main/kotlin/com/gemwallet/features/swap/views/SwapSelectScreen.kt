package com.gemwallet.features.swap.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.type
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.getBalanceInfo
import com.gemwallet.features.asset_select.presents.views.AssetSelectScene
import com.gemwallet.features.swap.viewmodels.SwapSelectViewModel
import com.gemwallet.features.swap.viewmodels.models.SwapItemType
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype

@Composable
fun SwapSelectScreen(
    onCancel: () -> Unit,
    onSelect: (select: SwapItemType, payId: AssetId?, receiveId: AssetId?) -> Unit,
    viewModel: SwapSelectViewModel = hiltViewModel()
) {
    val uiStates by viewModel.uiState.collectAsStateWithLifecycle()
    val pinned by viewModel.pinned.collectAsStateWithLifecycle()
    val unpinned by viewModel.unpinned.collectAsStateWithLifecycle()
    val availableChains by viewModel.availableChains.collectAsStateWithLifecycle()
    val chainsFilter by viewModel.chainFilter.collectAsStateWithLifecycle()
    val balanceFilter by viewModel.balanceFilter.collectAsStateWithLifecycle()
    val select by viewModel.select.collectAsStateWithLifecycle()
    val payId by viewModel.payAssetId.collectAsStateWithLifecycle()
    val receiveId by viewModel.receiveAssetId.collectAsStateWithLifecycle()

    AssetSelectScene(
        title = when (select) {
            SwapItemType.Pay -> stringResource(id = R.string.swap_you_pay)
            SwapItemType.Receive -> stringResource(id = R.string.swap_you_receive)
            null -> ""
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
        onSelect = {
            when (select) {
                SwapItemType.Pay -> onSelect(SwapItemType.Pay, it, receiveId)
                SwapItemType.Receive -> onSelect(SwapItemType.Receive, payId, it)
                null -> return@AssetSelectScene
            }
        },
        onCancel = onCancel,
        onAddAsset = null,
        itemTrailing = { getBalanceInfo(it)() },
        support = {
            if (it.asset.id.type() == AssetSubtype.NATIVE) null else {
                @Composable { ListItemSupportText(it.asset.id.chain.asset().name) }
            }
        },
    )
}