package com.gemwallet.features.confirm.presents.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
        is ConfirmProperty.Destination.App -> R.string.wallet_connect_app
    }
    val recipientName = when (model) {
        is ConfirmProperty.Destination.Provider,
        is ConfirmProperty.Destination.Stake -> null
        is ConfirmProperty.Destination.Transfer -> model.domain
        is ConfirmProperty.Destination.App -> model.name
    }
    PropertyItem(
        title = {
            PropertyTitleText(title)
        },
        data = {
            Column(horizontalAlignment = Alignment.End) {
                if (recipientName == null) {
                    Row(horizontalArrangement = Arrangement.End) { PropertyDataText(model.data) }
                } else {
                    Row(horizontalArrangement = Arrangement.End) { PropertyDataText(recipientName) }
                }
            }
        },
        listPosition = listPosition,
    )
}