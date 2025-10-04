package com.gemwallet.features.swap.views.components

import androidx.compose.runtime.Composable
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.features.swap.viewmodels.models.SwapProperty

@Composable
fun SwapSlippage(slippage: SwapProperty.Slippage, listPosition: ListPosition) {
    PropertyItem(
        title = R.string.swap_slippage,
        info = InfoSheetEntity.Slippage,
        data = slippage.percentageFormatted,
        listPosition = listPosition,
    )
}