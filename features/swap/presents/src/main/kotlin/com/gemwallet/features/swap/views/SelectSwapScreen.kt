package com.gemwallet.features.swap.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
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
import kotlinx.coroutines.coroutineScope

@Composable
fun SelectSwapScreen(
    select: SwapItemType,
    payAssetId: AssetId?,
    receiveAssetId: AssetId?,
    onCancel: () -> Unit,
    onSelect: ((AssetId) -> Unit)?,
    viewModel: SwapSelectViewModel = hiltViewModel()
) {
    val uiStates by viewModel.uiState.collectAsStateWithLifecycle()
    val pinned by viewModel.pinned.collectAsStateWithLifecycle()
    val unpinned by viewModel.unpinned.collectAsStateWithLifecycle()
    val availableChains by viewModel.availableChains.collectAsStateWithLifecycle()
    val chainsFilter by viewModel.chainFilter.collectAsStateWithLifecycle()
    val balanceFilter by viewModel.balanceFilter.collectAsStateWithLifecycle()

    LaunchedEffect(select, payAssetId, receiveAssetId) {
        coroutineScope {
            viewModel.setPair(select, payAssetId, receiveAssetId)
        }
    }

    AssetSelectScene(
        title = when (select) {
            SwapItemType.Pay -> stringResource(id = R.string.swap_you_pay)
            SwapItemType.Receive -> stringResource(id = R.string.swap_you_receive)
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
        onSelect = { onSelect?.invoke(it) },
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