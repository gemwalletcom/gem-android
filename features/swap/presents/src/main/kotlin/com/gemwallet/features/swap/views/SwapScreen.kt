package com.gemwallet.features.swap.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.features.swap.viewmodels.SwapViewModel
import com.gemwallet.features.swap.viewmodels.models.SwapItemType
import com.gemwallet.features.swap.viewmodels.models.SwapState
import com.gemwallet.features.swap.views.dialogs.PriceImpactWarningDialog
import com.gemwallet.features.swap.views.dialogs.ProviderListDialog
import com.gemwallet.features.swap.views.dialogs.SelectSwapAssetDialog
import com.gemwallet.features.swap.views.dialogs.SwapDetailsDialog

@Composable
fun SwapScreen(
    viewModel: SwapViewModel = hiltViewModel(),
    onConfirm: (ConfirmParams) -> Unit,
    onCancel: () -> Unit,
) {
    val pay by viewModel.payAsset.collectAsStateWithLifecycle()
    val receive by viewModel.receiveAsset.collectAsStateWithLifecycle()
    val fromEquivalent by viewModel.payEquivalentFormatted.collectAsStateWithLifecycle()
    val toEquivalent by viewModel.toEquivalentFormatted.collectAsStateWithLifecycle()
    val swapState by viewModel.uiSwapScreenState.collectAsStateWithLifecycle()
    val currentProvider by viewModel.currentProvider.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val priceImpact by viewModel.priceImpact.collectAsStateWithLifecycle()
    val rate by viewModel.rate.collectAsStateWithLifecycle()
    val estimateTime by viewModel.estimateTime.collectAsStateWithLifecycle()
    val slippage by viewModel.slippage.collectAsStateWithLifecycle()
    val minReceive by viewModel.minReceive.collectAsStateWithLifecycle()

    val selectState = remember { mutableStateOf<SwapItemType?>(null) }
    val isShowProviderSelect = remember { mutableStateOf(false) }
    val isShowPriceImpactAlert = remember { mutableStateOf(false) }
    val isShowDetails = remember { mutableStateOf(false) }

    val onSwap: () -> Unit =  {
        when (swapState) {
            SwapState.Ready -> viewModel.swap(onConfirm)
            is SwapState.Error -> viewModel.refresh()
            else -> {}
        }
    }

    SwapScene(
        swapState = swapState,
        pay = pay,
        receive = receive,
        priceImpact = priceImpact,
        payEquivalent = fromEquivalent,
        receiveEquivalent = toEquivalent,
        rate = rate,
        isShowPriceImpactAlert = isShowPriceImpactAlert,
        selectState = selectState,
        switchSwap = viewModel::switchSwap,
        payValue = viewModel.payValue,
        receiveValue = viewModel.receiveValue,
        onCancel = onCancel,
        onDetails = { isShowDetails.value = true },
    ) {
        when (swapState) {
            SwapState.Ready -> viewModel.swap(onConfirm)
            is SwapState.Error -> viewModel.refresh()
            else -> {}
        }
    }

    PriceImpactWarningDialog(
        isShowPriceImpactAlert = isShowPriceImpactAlert,
        priceImpact = priceImpact,
        asset = pay?.asset,
        onContinue = onSwap,
    )

    SelectSwapAssetDialog(
        select = selectState,
        payAssetId = pay?.id(),
        receiveAssetId = receive?.id(),
        onSelect = viewModel::onSelect,
    )

    SwapDetailsDialog(
        estimateTime = estimateTime,
        provider = currentProvider,
        providers = providers,
        priceImpact = priceImpact,
        rate = rate,
        slippage = slippage,
        minReceive = minReceive,
        isShowProviderSelect = isShowProviderSelect,
        isShow = isShowDetails,
        isUpdated = swapState == SwapState.GetQuote,
    )

    ProviderListDialog(
        isShow = isShowProviderSelect,
        isUpdated = swapState == SwapState.GetQuote,
        currentProvider = currentProvider?.swapProvider?.id,
        providers = providers,
        onProviderSelect = viewModel::setProvider
    )
}