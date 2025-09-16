package com.gemwallet.features.swap.views.dialogs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.type
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.ListItemSupportText
import com.gemwallet.android.ui.components.list_item.getBalanceInfo
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.gemwallet.features.asset_select.presents.views.AssetSelectScene
import com.gemwallet.features.swap.viewmodels.SwapSelectViewModel
import com.gemwallet.features.swap.viewmodels.models.SwapItemType
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectSwapAssetDialog(
    select: MutableState<SwapItemType?>,
    payAssetId: AssetId?,
    receiveAssetId: AssetId?,
    onSelect: (SwapItemType, AssetId) -> Unit,
) {
    val selectType = select.value ?: return
    val sheetState = rememberModalBottomSheetState(true)
    val dismiss = fun () { select.value = null }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = dismiss,
    ) {
        SelectSwapScreen(
            select = selectType,
            payAssetId = payAssetId,
            receiveAssetId = receiveAssetId,
            onCancel = dismiss,
            onSelect = {
                onSelect(selectType, it)
                dismiss()
            },
        )
    }
}

@Composable
private fun SelectSwapScreen(
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
