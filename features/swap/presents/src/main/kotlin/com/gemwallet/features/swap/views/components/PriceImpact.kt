package com.gemwallet.features.swap.views.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.theme.pendingColor
import com.gemwallet.features.swap.viewmodels.models.PriceImpact
import com.gemwallet.features.swap.viewmodels.models.PriceImpactType

@Composable
fun PriceImpact(priceImpact: PriceImpact?) {
    priceImpact ?: return
    PropertyItem(
        title = {
            PropertyTitleText(
                R.string.swap_price_impact,
                info = InfoSheetEntity.PriceImpactInfo,
            )
        },
        data = {
            PropertyDataText(
                text = priceImpact.percentageFormatted,
                color = when (priceImpact.type) {
                    PriceImpactType.Positive,
                    PriceImpactType.Low -> MaterialTheme.colorScheme.tertiary
                    PriceImpactType.Medium -> pendingColor
                    PriceImpactType.High -> MaterialTheme.colorScheme.error
                }
            )
        }
    )
}