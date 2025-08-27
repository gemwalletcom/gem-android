package com.gemwallet.features.wallets.presents.views.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ext.getAddressEllipsisText
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.DropDownContextItem
import com.gemwallet.android.ui.components.list_item.WalletItem
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
    items(items = wallets, key = { it.id }) { wallet ->
        DropDownContextItem(
            isExpanded = longPressedWallet.value == wallet.id,
            imeCompensate = true,
            onDismiss = { longPressedWallet.value = "" },
            content = {
                WalletItem(
                    id = wallet.id,
                    name = wallet.name,
                    icon = wallet.icon,
                    typeLabel = when (wallet.type) {
                        WalletType.multicoin -> stringResource(id = R.string.wallet_multicoin)
                        WalletType.single -> wallet.typeLabel.getAddressEllipsisText()
                        else -> wallet.typeLabel.getAddressEllipsisText()
                    },
                    isCurrent = wallet.id == currentWalletId,
                    type = wallet.type,
                    onEdit = { walletId -> onEdit(walletId) },
                )
            },
            menuItems = {
                WalletDropDownItem(
                    if (wallet.isPinned) R.string.common_unpin else R.string.common_pin,
                    if (wallet.isPinned) R.drawable.keep_off else Icons.Default.PushPin,
                ) {
                    onTogglePin(wallet.id)
                    longPressedWallet.value = ""
                }
                WalletDropDownItem(R.string.common_wallet, Icons.Default.Settings) {
                    onEdit(wallet.id)
                    longPressedWallet.value = ""
                }
                WalletDropDownItem(R.string.common_delete, Icons.Default.Delete, MaterialTheme.colorScheme.error) {
                    onDeleteWallet(wallet.id)
                    longPressedWallet.value = ""
                }
            },
            onLongClick = { longPressedWallet.value = wallet.id }
        ) { onSelectWallet(wallet.id) }
    }
    if (isPinned) {
        item { Spacer(modifier = Modifier.height(32.dp)) }
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