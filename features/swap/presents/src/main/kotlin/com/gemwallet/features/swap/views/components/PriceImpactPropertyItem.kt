package com.gemwallet.features.swap.views.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.pendingColor
import com.gemwallet.features.swap.viewmodels.models.PriceImpactType
import com.gemwallet.features.swap.viewmodels.models.SwapProperty

@Composable
fun PriceImpactPropertyItem(priceImpact: SwapProperty.PriceImpact, listPosition: ListPosition) {
    PropertyItem(
        title = R.string.swap_price_impact,
        info = InfoSheetEntity.PriceImpactInfo,
        data = priceImpact.percentageFormatted,
        dataColor = when (priceImpact.type) {
            PriceImpactType.Positive,
            PriceImpactType.Low -> MaterialTheme.colorScheme.tertiary
            PriceImpactType.Medium -> pendingColor
            PriceImpactType.High -> MaterialTheme.colorScheme.error
        },
        listPosition = listPosition,
    )
}