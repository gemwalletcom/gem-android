package com.gemwallet.features.swap.views.components

import androidx.compose.runtime.Composable
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.features.swap.viewmodels.models.SlippageModel

@Composable
fun SwapSlippage(slippage: SlippageModel?) {
    slippage ?: return
    PropertyItem(
        title = {
            PropertyTitleText(
                R.string.swap_slippage,
                info = InfoSheetEntity.Slippage,
            )
        },
        data = { PropertyDataText(slippage.percentageFormatted) }
    )
}