package com.gemwallet.features.swap.views

// import com.gemwallet.android.features.confirm.views.ConfirmScreen
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.features.swap.viewmodels.SwapViewModel
import com.gemwallet.features.swap.viewmodels.models.SwapItemType
import com.gemwallet.features.swap.viewmodels.models.SwapPairSelect
import com.gemwallet.features.swap.viewmodels.models.SwapRate
import com.gemwallet.features.swap.viewmodels.models.SwapState
import com.gemwallet.features.swap.views.components.CurrentSwapProvider
import com.gemwallet.features.swap.views.components.PriceImpact
import com.gemwallet.features.swap.views.components.SwapError
import com.gemwallet.features.swap.views.components.SwapItem

@Composable
fun SwapScreen(
    viewModel: SwapViewModel = hiltViewModel(),
    onConfirm: (ConfirmParams) -> Unit,
    onBuy: AssetIdAction,
    onCancel: () -> Unit,
) {
    val selectState by viewModel.selectPair.collectAsStateWithLifecycle()
    val pay by viewModel.payAsset.collectAsStateWithLifecycle()
    val receive by viewModel.receiveAsset.collectAsStateWithLifecycle()
    val fromEquivalent by viewModel.fromEquivalentFormatted.collectAsStateWithLifecycle()
    val toEquivalent by viewModel.toEquivalentFormatted.collectAsStateWithLifecycle()
    val swapState by viewModel.swapScreenState.collectAsStateWithLifecycle()
    val currentProvider by viewModel.currentProvider.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val priceImpact by viewModel.priceImpact.collectAsStateWithLifecycle()
    val rate by viewModel.rate.collectAsStateWithLifecycle()
    val estimateTime by viewModel.estimateTime.collectAsStateWithLifecycle()

    val isShowProviderSelect = remember { mutableStateOf(false) }
    var approveParams by rememberSaveable { mutableStateOf<ConfirmParams?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    var isShowPriceImpactAlert by remember { mutableStateOf(false) }

    val onSwap: () -> Unit =  {
        when (swapState) {
            SwapState.Ready -> viewModel.swap(onConfirm)
            SwapState.RequestApprove -> viewModel.swap { approveParams = it }
            is SwapState.Error -> viewModel.refresh()
            else -> {}
        }
    }

    BackHandler(selectState != null) {
        if (approveParams != null) {
            approveParams = null
            return@BackHandler
        }

        if (pay == null && receive == null) {
            onCancel()
        } else {
            viewModel.onSelect(SwapPairSelect.request(pay?.id(), receive?.id()))
        }
    }

    Scene(
        title = stringResource(id = R.string.wallet_swap),
        mainAction = {
            SwapAction(swapState, pay) {
                isShowPriceImpactAlert = priceImpact?.isHigh == true
                if (!isShowPriceImpactAlert) {
                    onSwap()
                }
            }
        },
        onClose = onCancel,
    ) {
        SwapItem(
            type = SwapItemType.Pay,
            item = pay,
            equivalent = fromEquivalent,
            state = viewModel.payValue,
            onAssetSelect = {
                keyboardController?.hide()
                viewModel.changePair(it)
            }
        )
        IconButton(onClick = viewModel::switchSwap) {
            Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = "swap_switch"
            )
        }
        SwapItem(
            type = SwapItemType.Receive,
            item = receive,
            equivalent = toEquivalent,
            state = viewModel.receiveValue,
            calculating = swapState == SwapState.GetQuote,
            onAssetSelect = {
                keyboardController?.hide()
                viewModel.changePair(it)
            }
        )
        currentProvider?.let { provider ->
            CurrentSwapProvider(provider, providers.size > 1, isShowProviderSelect)
        }
        estimateTime?.let {
            PropertyItem(
                title = { PropertyTitleText(R.string.swap_estimated_time_title) },
                data = { PropertyDataText("\u2248 $it min") }
            )
        }
        SwapRate(rate)
        PriceImpact(priceImpact)
        Spacer16()
        SwapError(swapState)
    }

    if (isShowPriceImpactAlert) {
        PriceImpactWarningDialog(
            priceImpact = priceImpact,
            asset = pay?.asset,
            onDismiss = { isShowPriceImpactAlert = false }) {
            isShowPriceImpactAlert = false
            onSwap()
        }
    }

    AnimatedVisibility(
        visible = approveParams != null,
        enter = slideIn { IntOffset(it.width, 0) },
        exit = slideOut { IntOffset(it.width, 0) },
    ) {
        LocalSoftwareKeyboardController.current?.hide()
//        ConfirmScreen(
//            approveParams ?: return@AnimatedVisibility,
//            finishAction = { assetId, hash, route ->
//                approveParams = null
//                viewModel.onTxHash(hash)
//            },
//            onBuy = onBuy,
//            cancelAction = {
//                approveParams = null
//            },
//        )
    }

    AnimatedVisibility(
        visible = selectState != null,
        enter = slideIn { IntOffset(it.width, 0) },
        exit = slideOut { IntOffset(it.width, 0) },
    ) {
        SelectSwapScreen(
            select = selectState ?: return@AnimatedVisibility,
            onCancel = {
                if (pay == null || receive == null) {
                    onCancel()
                } else {
                    viewModel.onSelect(SwapPairSelect.request(pay?.id(), receive?.id()))
                }
            },
            onSelect = {select -> viewModel.onSelect(select) },
        )
    }
    ProviderListDialog(
        isShow = isShowProviderSelect,
        isUpdated = swapState == SwapState.GetQuote,
        currentProvider = currentProvider?.swapProvider?.id,
        providers = providers,
        onProviderSelect = viewModel::setProvider
    )
}

@Composable
internal fun SwapRate(rate: SwapRate?) {
    var direction by remember { mutableStateOf(false) }

    rate ?: return

    PropertyItem(
        title = { PropertyTitleText(R.string.buy_rate) },
        data = {
            PropertyDataText(
                text = when (direction) {
                    true -> rate.reverse
                    false -> rate.forward
                },
                badge = {
                    Icon(
                        modifier = Modifier.clickable(onClick = { direction = !direction }),
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            )
        }
    )
}