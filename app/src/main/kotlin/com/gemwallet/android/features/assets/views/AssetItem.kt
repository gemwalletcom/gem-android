package com.gemwallet.android.features.assets.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.AssetListItem
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.list_item.DropDownContextItem
import com.gemwallet.android.ui.components.list_item.AssetItemUIModel
import com.wallet.core.primitives.AssetId

@Composable
internal fun AssetItem(
    item: AssetItemUIModel,
    longPressState: MutableState<AssetId?>,
    modifier: Modifier = Modifier,
    isPinned: Boolean = false,
    onAssetClick: (AssetId) -> Unit,
    onAssetHide: (AssetId) -> Unit,
    onTogglePin: (AssetId) -> Unit,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current.nativeClipboard
    DropDownContextItem(
        modifier = modifier.testTag(item.asset.id.toIdentifier()),
        isExpanded = longPressState.value == item.asset.id,
        imeCompensate = false,
        onDismiss = { longPressState.value = null },
        content = { AssetListItem(item) },
        menuItems = {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = if (isPinned) R.string.common_unpin else R.string.common_pin)) },
                trailingIcon = {
                    if (isPinned) Icon(painterResource(R.drawable.keep_off), "unpin")
                    else Icon(Icons.Default.PushPin, "pin")

                },
                onClick = {
                    onTogglePin(item.asset.id)
                    longPressState.value = null
                },
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.wallet_copy_address)) },
                trailingIcon = { Icon(Icons.Default.ContentCopy, "copy") },
                onClick = {
                    clipboardManager.setPlainText(context, item.owner ?: "")
                    longPressState.value = null
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.common_hide)) },
                trailingIcon = { Icon(Icons.Default.VisibilityOff, "wallet_config") },
                onClick = {
                    onAssetHide(item.asset.id)
                    longPressState.value = null
                }
            )
        },
        onLongClick = { longPressState.value = item.asset.id }
    ) { onAssetClick(item.asset.id) }
}