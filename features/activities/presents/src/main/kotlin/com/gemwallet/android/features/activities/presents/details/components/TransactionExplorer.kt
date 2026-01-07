package com.gemwallet.android.features.activities.presents.details.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.open

@Composable
internal fun TransactionExplorer(explorerName: String, uri: String) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    PropertyItem(
        modifier = Modifier.Companion.clickable { uriHandler.open(context, uri) },
        title = {
            PropertyTitleText(
                stringResource(
                    id = R.string.transaction_view_on,
                    explorerName
                )
            )
        },
        data = {
            PropertyDataText(
                text = "",
                badge = {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowForwardIos,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            )
        },
        listPosition = ListPosition.Single
    )
}