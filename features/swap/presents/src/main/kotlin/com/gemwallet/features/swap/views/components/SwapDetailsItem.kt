package com.gemwallet.features.swap.views.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clickable
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.features.swap.viewmodels.models.SwapRate

@Composable
fun SwapDetailItem(
    rate: SwapRate?,
    onClick: () -> Unit,
) {
    var direction by remember { mutableStateOf(false) }

    rate ?: return

    PropertyItem(
        modifier = Modifier.clickable(onClick),
        title = { PropertyTitleText(R.string.common_details) },
        data = {
            PropertyDataText(
                text = when (direction) {
                    true -> rate.reverse
                    false -> rate.forward
                },
                badge = { DataBadgeChevron() }
            )
        }
    )
}