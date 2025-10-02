package com.gemwallet.features.wallets.presents.views.components

import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ext.getAddressEllipsisText
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.DropDownContextItem
import com.gemwallet.android.ui.components.list_item.WalletItem
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.features.wallets.viewmodels.cases.icon
import com.gemwallet.features.wallets.viewmodels.cases.typeLabel
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType

internal fun LazyListScope.wallets(
    wallets: List<Wallet>,
    currentWalletId: String,
    longPressedWallet: MutableState<String>,
    isPinned: Boolean = false,
    onEdit: (String) -> Unit,
    onSelectWallet: (String) -> Unit,
    onDeleteWallet: (String) -> Unit,
    onTogglePin: (String) -> Unit,
) {
    if (isPinned && wallets.isNotEmpty()) {
        pinnedHeader()
    }
    itemsIndexed(items = wallets, key = { index, item -> item.id }) { index, item ->
        DropDownContextItem(
            isExpanded = longPressedWallet.value == item.id,
            imeCompensate = true,
            onDismiss = { longPressedWallet.value = "" },
            content = {
                WalletItem(
                    id = item.id,
                    name = item.name,
                    icon = item.icon,
                    typeLabel = when (item.type) {
                        WalletType.multicoin -> stringResource(id = R.string.wallet_multicoin)
                        WalletType.single -> item.typeLabel.getAddressEllipsisText()
                        else -> item.typeLabel.getAddressEllipsisText()
                    },
                    isCurrent = item.id == currentWalletId,
                    type = item.type,
                    listPosition = ListPosition.getPosition(index, wallets.size),
                    onEdit = { walletId -> onEdit(walletId) },
                    modifier = it
                )
            },
            menuItems = {
                WalletDropDownItem(
                    if (item.isPinned) R.string.common_unpin else R.string.common_pin,
                    if (item.isPinned) R.drawable.keep_off else Icons.Default.PushPin,
                ) {
                    onTogglePin(item.id)
                    longPressedWallet.value = ""
                }
                WalletDropDownItem(R.string.common_wallet, Icons.Default.Settings) {
                    onEdit(item.id)
                    longPressedWallet.value = ""
                }
                WalletDropDownItem(R.string.common_delete, Icons.Default.Delete, MaterialTheme.colorScheme.error) {
                    onDeleteWallet(item.id)
                    longPressedWallet.value = ""
                }
            },
            onLongClick = { longPressedWallet.value = item.id }
        ) { onSelectWallet(item.id) }
    }
}

@Composable
private fun WalletDropDownItem(
    @StringRes text: Int,
    icon: Any,
    color: Color = Color.Unspecified,
    onClick: () -> Unit,
) {
    val text = stringResource(text)
    DropdownMenuItem(
        text = {
            Text(text = text, color = color)
        },
        trailingIcon = {
            when (icon) {
                is ImageVector -> Icon(
                    imageVector = icon,
                    tint = color,
                    contentDescription = text
                )
                is Int -> Icon(painterResource(icon), text)
            }

        },
        onClick = onClick,
    )
}