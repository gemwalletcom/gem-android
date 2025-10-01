package com.gemwallet.android.features.activities.presents.details.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.gemwallet.android.features.activities.models.TxDetailsProperty
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.Spacer8

@Composable
fun DestinationPropertyItem(property: TxDetailsProperty.Destination, listPosition: ListPosition) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current.nativeClipboard
    val title = when (property) {
        is TxDetailsProperty.Destination.Recipient -> R.string.transaction_recipient
        is TxDetailsProperty.Destination.Sender -> R.string.transaction_sender
    }

    PropertyItem(
        title = { PropertyTitleText(title) },
        data = {
            Row(
                modifier = Modifier
                    .clickable { clipboardManager.setPlainText(context, property.data) }
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = property.data,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer8()
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = ""
                )
            }
        },
        listPosition = listPosition,
    )
}