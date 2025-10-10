package com.gemwallet.features.swap.views.components

import androidx.compose.material3.Text
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
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.features.swap.viewmodels.models.PriceImpactType
import com.gemwallet.features.swap.viewmodels.models.SwapProperty
import com.gemwallet.features.swap.viewmodels.models.SwapState

@Composable
fun SwapDetailPropertyItem(
    rate: SwapProperty.Rate?,
    priceImpact: SwapProperty.PriceImpact?,
    swapState: SwapState,
    onClick: () -> Unit,
) {
    var direction by remember { mutableStateOf(false) }

    if (rate == null || swapState == SwapState.GetQuote) {
        return
    }

    PropertyItem(
        modifier = Modifier.clickable(onClick),
        title = { PropertyTitleText(R.string.common_details) },
        data = {
            PropertyDataText(
                text = when (direction) {
                    true -> rate.reverse
                    false -> rate.forward
                },
                badge = {
                    DataBadgeChevron {
                        when (priceImpact?.type) {
                            null,
                            PriceImpactType.Low -> {}
                            PriceImpactType.Medium,
                            PriceImpactType.High,
                            PriceImpactType.Positive -> Text(
                                text = "(${priceImpact.percentageFormatted})",
                                color = priceImpact.getColor(),
                            )
                        }
                    }
                }
            )
        },
        listPosition = ListPosition.Single,
    )
}