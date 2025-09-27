package com.gemwallet.features.asset.presents.details.views.components

import android.content.Intent
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.open
import com.gemwallet.features.asset.viewmodels.details.models.AssetInfoUIModel
import com.wallet.core.primitives.AssetId
import kotlinx.coroutines.launch

@Composable
fun RowScope.AssetDetailsMenu(
    uiState: AssetInfoUIModel,
    priceAlertEnabled: Boolean,
    snackBar: SnackbarHostState,
    onPriceAlert: (AssetId) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val priceAlertToastRes = if (priceAlertEnabled) R.string.price_alerts_disabled_for else R.string.price_alerts_enabled_for
    val priceAlertToastMessage = stringResource(priceAlertToastRes, uiState.asset.name)
    var menuExpanded by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val shareTitle = stringResource(id = R.string.common_share)

    val onShare = fun () {
        val type = "text/plain"
        val subject = "${uiState.assetInfo.owner?.chain}\n${uiState.assetInfo.asset.symbol}"

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = type
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, uiState.assetInfo.owner?.address)

        context.startActivity(Intent.createChooser(intent, shareTitle))
    }

    IconButton(
        onClick = {
            onPriceAlert(uiState.asset.id)
            scope.launch { snackBar.showSnackbar(message = priceAlertToastMessage) }
        }
    ) {
        if (priceAlertEnabled) {
            Icon(Icons.Default.Notifications, "")
        } else {
            Icon(Icons.Default.NotificationsNone, "")
        }
    }
    IconButton(onClick = { menuExpanded = !menuExpanded }) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "More",
        )
    }
    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false },
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        uiState.explorerAddressUrl?.let {
            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.asset_view_address_on, uiState.explorerName))
                },
                onClick = { uriHandler.open(context, it) },
            )

        }
        DropdownMenuItem(
            text = {
                Text(stringResource(R.string.common_share))
            },
            onClick = onShare,
        )
    }
}