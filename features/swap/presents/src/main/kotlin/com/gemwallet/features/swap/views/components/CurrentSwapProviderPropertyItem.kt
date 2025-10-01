package com.gemwallet.features.swap.views.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.features.swap.viewmodels.models.SwapProviderItem

@Composable
internal fun CurrentSwapProviderPropertyItem(
    provider: SwapProviderItem,
    isAvailableChoose: Boolean,
    isShowProviderSelect: MutableState<Boolean>,
) {
    val modifier = if (isAvailableChoose) {
        Modifier.clickable { isShowProviderSelect.value = true }
    } else {
        Modifier
    }
    PropertyItem(
        modifier = modifier,
        title = { PropertyTitleText(R.string.common_provider) },
        data = {
            PropertyDataText(
                text = provider.swapProvider.name,
                badge = { DataBadgeChevron(provider.icon, isAvailableChoose) }
            )
        },
        listPosition = ListPosition.Single,
    )
}