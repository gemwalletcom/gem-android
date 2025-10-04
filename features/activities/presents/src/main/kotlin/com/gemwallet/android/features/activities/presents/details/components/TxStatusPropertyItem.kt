package com.gemwallet.android.features.activities.presents.details.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.domains.asset.getIconUrl
import com.gemwallet.android.features.activities.models.TxDetailsProperty
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.InfoSheetEntity
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.pendingColor
import com.wallet.core.primitives.TransactionState

@Composable
fun TxStatusPropertyItem(property: TxDetailsProperty.Status, position: ListPosition) {
    PropertyItem(
        title = {
            PropertyTitleText(R.string.transaction_status, info = InfoSheetEntity.TransactionInfo(icon = property.asset.getIconUrl(), state = property.data))
        },
        data = {
            PropertyDataText(
                text = when (property.data) {
                    TransactionState.Pending -> stringResource(id = R.string.transaction_status_pending)
                    TransactionState.Confirmed -> stringResource(id = R.string.transaction_status_confirmed)
                    TransactionState.Failed -> stringResource(id = R.string.transaction_status_failed)
                    TransactionState.Reverted -> stringResource(id = R.string.transaction_status_reverted)
                },
                color = when (property.data) {
                    TransactionState.Pending -> pendingColor
                    TransactionState.Confirmed -> MaterialTheme.colorScheme.tertiary
                    TransactionState.Failed,
                    TransactionState.Reverted -> MaterialTheme.colorScheme.error
                },
                badge = {
                    Spacer8()
                    when (property.data) {
                        TransactionState.Pending -> CircularProgressIndicator16(color = pendingColor)
                        else -> null
                    }
                },
            )
        },
        listPosition = position,
    )
}