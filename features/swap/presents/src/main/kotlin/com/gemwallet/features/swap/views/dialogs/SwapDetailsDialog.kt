package com.gemwallet.features.swap.views.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator20
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.gemwallet.android.ui.models.getListPosition
import com.gemwallet.features.swap.viewmodels.models.SwapProperty
import com.gemwallet.features.swap.viewmodels.models.SwapProviderItem
import com.gemwallet.features.swap.views.components.CurrentSwapProviderPropertyItem
import com.gemwallet.features.swap.views.components.PriceImpactPropertyItem
import com.gemwallet.features.swap.views.components.SwapRatePropertyItem
import com.gemwallet.features.swap.views.components.SwapSlippage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwapDetailsDialog(
    isShow: MutableState<Boolean>,
    isUpdated: Boolean,
    providers: List<SwapProviderItem?>,
    provider: SwapProviderItem?,
//    estimateTime: String?,
//    priceImpact: PriceImpact?,
//    slippage: SlippageModel?,
//    minReceive: String?,
//    rate: SwapRate?,
    properties: List<SwapProperty>,
    isShowProviderSelect: MutableState<Boolean>,
) {
    if (!isShow.value) {
        return
    }

    ModalBottomSheet(onDismissRequest = { isShow.value = false }, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        val configuration = LocalWindowInfo.current
        val density = LocalDensity.current
        val minHeight = with(density) {
            (configuration.containerSize.height * 0.6f).toDp()
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
                    SubheaderItem(stringResource(R.string.common_provider))
                    provider?.let { provider ->
                        CurrentSwapProviderPropertyItem(provider, providers.size > 1, isShowProviderSelect)
                    }
                }
                itemsIndexed(properties) { index, item ->
                    val position = properties.getListPosition(index)
                    when (item) {
                        is SwapProperty.Estimate -> PropertyItem(
                            R.string.swap_estimated_time_title,
                            "\u2248 ${item.data} min",
                            listPosition = position,
                        )
                        is SwapProperty.MinReceive -> PropertyItem(R.string.swap_min_receive, item.data, listPosition = position)
                        is SwapProperty.PriceImpact -> PriceImpactPropertyItem(item, position)
                        is SwapProperty.Rate -> SwapRatePropertyItem(item, position)
                        is SwapProperty.Slippage -> SwapSlippage(item, position)
                    }
                }
//                item {
//                    com.gemwallet.features.swap.views.components.SwapRate(rate)
//                    estimateTime?.let {
//                        PropertyItem(
//                            title = { PropertyTitleText(R.string.swap_estimated_time_title) },
//                            data = { PropertyDataText("\u2248 $it min") }
//                        )
//                    }
//                    PriceImpact(priceImpact)
//                    SwapMinReceive(minReceive)
//                    SwapSlippage(slippage)
//                    Spacer16()
//                }
            }
        }
    }
}

