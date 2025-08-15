package com.gemwallet.features.swap.views

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.features.swap.viewmodels.models.PriceImpact
import com.gemwallet.features.swap.viewmodels.models.SwapItemType
import com.gemwallet.features.swap.viewmodels.models.SwapProviderItem
import com.gemwallet.features.swap.viewmodels.models.SwapRate
import com.gemwallet.features.swap.viewmodels.models.SwapState
import com.gemwallet.features.swap.views.components.CurrentSwapProvider
import com.gemwallet.features.swap.views.components.PriceImpact
import com.gemwallet.features.swap.views.components.SwapAction
import com.gemwallet.features.swap.views.components.SwapError
import com.gemwallet.features.swap.views.components.SwapItem

@Composable
internal fun SwapScene(
    swapState: SwapState,
    pay: AssetInfo?,
    receive: AssetInfo?,
    payEquivalent: String,
    receiveEquivalent: String,
    priceImpact: PriceImpact?,
    rate: SwapRate?,
    estimateTime: String?,
    providers: List<SwapProviderItem?>,
    provider: SwapProviderItem?,
    isShowPriceImpactAlert: MutableState<Boolean>,
    selectState: MutableState<SwapItemType?>,
    isShowProviderSelect: MutableState<Boolean>,
    payValue: TextFieldState,
    receiveValue: TextFieldState,
    switchSwap: () -> Unit,
    onCancel: () -> Unit,
    onSwap: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Scene(
        title = stringResource(id = R.string.wallet_swap),
        mainAction = {
            SwapAction(swapState, pay) {
                if (priceImpact?.isHigh == true) {
                    isShowPriceImpactAlert.value = true
                } else {
                    onSwap()
                }
            }
        },
        onClose = onCancel,
    ) {
        SwapItem(
            type = SwapItemType.Pay,
            item = pay,
            equivalent = payEquivalent,
            state = payValue,
            onAssetSelect = {
                keyboardController?.hide()
                selectState.value = SwapItemType.Pay
            }
        )
        IconButton(onClick = switchSwap) {
            Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = "swap_switch"
            )
        }
        SwapItem(
            type = SwapItemType.Receive,
            item = receive,
            equivalent = receiveEquivalent,
            state = receiveValue,
            calculating = swapState == SwapState.GetQuote,
            onAssetSelect = {
                keyboardController?.hide()
                selectState.value = SwapItemType.Receive
            }
        )
        provider?.let { provider ->
            CurrentSwapProvider(provider, providers.size > 1, isShowProviderSelect)
        }
        estimateTime?.let {
            PropertyItem(
                title = { PropertyTitleText(R.string.swap_estimated_time_title) },
                data = { PropertyDataText("\u2248 $it min") }
            )
        }
        com.gemwallet.features.swap.views.components.SwapRate(rate)
        PriceImpact(priceImpact)
        Spacer16()
        SwapError(swapState)
    }
}