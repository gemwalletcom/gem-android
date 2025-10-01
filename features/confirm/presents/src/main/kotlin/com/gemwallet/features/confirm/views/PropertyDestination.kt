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
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.features.confirm.models.ConfirmProperty

@Composable
fun PropertyDestination(
    model: ConfirmProperty.Destination?,
    listPosition: ListPosition,
) {
    model ?: return

    val title = when (model) {
        is ConfirmProperty.Destination.Provider -> R.string.common_provider
        is ConfirmProperty.Destination.Stake -> R.string.stake_validator
        is ConfirmProperty.Destination.Transfer -> R.string.transaction_recipient
    }
    val domain = when (model) {
        is ConfirmProperty.Destination.Provider,
        is ConfirmProperty.Destination.Stake -> null
        is ConfirmProperty.Destination.Transfer -> model.domain
    }
    PropertyItem(
        modifier = Modifier.height(if (domain.isNullOrEmpty()) 56.dp else 72.dp),
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
        },
        listPosition = listPosition,
    )
}