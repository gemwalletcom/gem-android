package com.gemwallet.android.features.wallet.presents.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingSmall

@Composable
internal fun WalletAddress(
    addresses: List<String>,
) {
    // Show if single account wallet
    val address = addresses.takeIf { it.size == 1 }?.firstOrNull() ?: return

    var isDropDownShow by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboard.current.nativeClipboard
    val context = LocalContext.current

    PropertyItem(
        modifier = Modifier.combinedClickable(
            enabled = true,
            onClick = {},
            onLongClick = { isDropDownShow = true }
        ),
        title = { PropertyTitleText(R.string.common_address) },
        data = {
            PropertyDataText(
                modifier = Modifier.weight(1f),
                text = address,
            )
        },
        listPosition = ListPosition.Single,
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        DropdownMenu(
            modifier = Modifier.align(Alignment.BottomEnd),
            expanded = isDropDownShow,
            offset = DpOffset(paddingDefault, paddingSmall),
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { isDropDownShow = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.wallet_copy_address)) },
                trailingIcon = { Icon(Icons.Default.ContentCopy, "copy") },
                onClick = {
                    isDropDownShow = false
                    clipboardManager.setPlainText(context, address)
                },
            )
        }
    }
}