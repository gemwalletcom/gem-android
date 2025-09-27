package com.gemwallet.features.asset.presents.details.views.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.priceColor
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.features.asset.viewmodels.details.models.AssetInfoUIModel
import com.wallet.core.primitives.AssetId

internal fun LazyListScope.price(
    uiState: AssetInfoUIModel,
    onChart: (AssetId) -> Unit,
) {
    item {
        PropertyItem(
            modifier = Modifier.clickable { onChart(uiState.asset.id) }
                .testTag("assetChart"),
            title = { PropertyTitleText(R.string.asset_price) },
            data = {
                PropertyDataText(
                    text = uiState.priceValue,
                    badge = {
                        DataBadgeChevron {
                            Text(
                                text = uiState.priceDayChanges,
                                color = priceColor(uiState.priceChangedType),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                )
            }
        )
    }
}