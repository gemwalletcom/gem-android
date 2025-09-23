package com.gemwallet.features.swap.views.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator20
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.features.swap.viewmodels.models.PriceImpact
import com.gemwallet.features.swap.viewmodels.models.SlippageModel
import com.gemwallet.features.swap.viewmodels.models.SwapProviderItem
import com.gemwallet.features.swap.viewmodels.models.SwapRate
import com.gemwallet.features.swap.views.components.CurrentSwapProvider
import com.gemwallet.features.swap.views.components.PriceImpact
import com.gemwallet.features.swap.views.components.SwapMinReceive
import com.gemwallet.features.swap.views.components.SwapSlippage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwapDetailsDialog(
    isShow: MutableState<Boolean>,
    isUpdated: Boolean,
    estimateTime: String?,
    providers: List<SwapProviderItem?>,
    provider: SwapProviderItem?,
    priceImpact: PriceImpact?,
    slippage: SlippageModel?,
    minReceive: String?,
    rate: SwapRate?,
    isShowProviderSelect: MutableState<Boolean>,
) {
    if (!isShow.value) {
        return
    }

    ModalBottomSheet(onDismissRequest = { isShow.value = false }, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        val configuration = LocalWindowInfo.current
        val density = LocalDensity.current
        val minHeight = with(density) {
            (configuration.containerSize.height * 0.5f).toDp()
        }
        Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = minHeight)
        ) {
            if (isUpdated) {
                CircularProgressIndicator20(modifier = Modifier.align(Alignment.Center))
                return@ModalBottomSheet
            }
            LazyColumn {
                item {
                    provider?.let { provider ->
                        CurrentSwapProvider(provider, providers.size > 1, isShowProviderSelect)
                    }
                    SubheaderItem(stringResource(R.string.common_details))
                    com.gemwallet.features.swap.views.components.SwapRate(rate)
                    estimateTime?.let {
                        PropertyItem(
                            title = { PropertyTitleText(R.string.swap_estimated_time_title) },
                            data = { PropertyDataText("\u2248 $it min") }
                        )
                    }
                    PriceImpact(priceImpact)
                    SwapMinReceive(minReceive)
                    SwapSlippage(slippage)
                    Spacer16()
                }
            }
        }
    }
}

