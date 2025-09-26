package com.gemwallet.android.ui.components.list_item

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.domains.asset.getIconUrl
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.Spacer8
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType

@Composable
fun WalletItem(
    wallet: Wallet,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
    onEdit: ((String) -> Unit)? = null,
) {
    WalletItem(
        modifier = modifier,
        id = wallet.id,
        name = wallet.name,
        typeLabel = when (wallet.type) {
            WalletType.private_key,
            WalletType.view,
            WalletType.single -> wallet.accounts.firstOrNull()?.address?.substring(0, 10) ?: ""
            WalletType.multicoin -> "Multi-coin"
        },
        icon = if (wallet.accounts.size > 1) {
            R.drawable.multicoin_wallet
        } else {
            wallet.accounts.firstOrNull()?.chain?.getIconUrl() ?: ""
        },
        isCurrent = isCurrent,
        type = wallet.type,
        onEdit = onEdit
    )
}

@Composable
fun WalletItem(
    id: String,
    name: String,
    typeLabel: String,
    icon: Any?,
    isCurrent: Boolean,
    type: WalletType,
    modifier: Modifier = Modifier,
    onEdit: ((String) -> Unit)? = null,
) {
    ListItem(
        modifier = modifier.heightIn(72.dp),
        leading = @Composable {
            IconWithBadge(
                icon = icon,
                supportIcon = if (type == WalletType.view) {
                    "android.resource://com.gemwallet.android/drawable/${R.drawable.watch_badge}"
                } else null,
            )
        },
        title = {
            ListItemTitleText(
                text = name,
                titleBadge = if (type == WalletType.view) {
                    { Badge(stringResource(id = R.string.wallets_watch).uppercase()) }
                } else {
                    null
                }
            )
        },
        subtitle = { ListItemSupportText(typeLabel) },
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer16()
                if (isCurrent) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "checked",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                if (onEdit != null) {
                    Spacer8()
                    IconButton(onClick = { onEdit(id) }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "edit",
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun PreviewWalletItem() {
    MaterialTheme {
        WalletItem(
            id = "1",
            name = "Foo wallet name",
            icon = "",
            typeLabel = "Multi-coin",
            type = WalletType.multicoin,
            isCurrent = true,
            onEdit = {},
        )
    }
}