package com.gemwallet.features.swap.views.components

import androidx.compose.runtime.Composable
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText

@Composable
fun SwapMinReceive(minReceive: String?) {
    minReceive ?: return
    PropertyItem(
        title = {
            PropertyTitleText(
                R.string.swap_min_receive,
            )
        },
        data = { PropertyDataText(minReceive) }
    )
}