package com.gemwallet.features.confirm.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.features.confirm.models.DestinationUIModel

@Composable
fun PropertyDestination(model: DestinationUIModel?) {
    model ?: return
    val title = when (model) {
        is DestinationUIModel.Provider -> R.string.common_provider
        is DestinationUIModel.Stake -> R.string.stake_validator
        is DestinationUIModel.Transfer -> R.string.transaction_recipient
    }
    val domain = when (model) {
        is DestinationUIModel.Provider,
        is DestinationUIModel.Stake -> null
        is DestinationUIModel.Transfer -> model.domain
    }
    PropertyItem(
        modifier = Modifier.height(72.dp),
        title = {
            PropertyTitleText(title)
        },
        data = {
            Column(horizontalAlignment = Alignment.End) {
                domain?.let {
                    Row(horizontalArrangement = Arrangement.End) { PropertyDataText(it) }
                }
                Row(horizontalArrangement = Arrangement.End) { PropertyDataText(model.data) }
            }
        }
    )
}