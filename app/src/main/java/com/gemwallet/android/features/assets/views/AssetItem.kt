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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.gemwallet.android.R
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ui.components.AssetListItem
import com.gemwallet.android.ui.components.DropDownContextItem
import com.gemwallet.android.ui.models.AssetItemUIModel
import com.wallet.core.primitives.AssetId

@Composable
internal fun AssetItem(
    item: AssetItemUIModel,
    longPressState: MutableState<AssetId?>,
    modifier: Modifier = Modifier.Companion,
    isPinned: Boolean = false,
    onAssetClick: (AssetId) -> Unit,
    onAssetHide: (AssetId) -> Unit,
    onTogglePin: (AssetId) -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
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
                    clipboardManager.setText(AnnotatedString(item.owner))
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