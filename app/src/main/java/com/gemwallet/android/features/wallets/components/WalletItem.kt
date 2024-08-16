package com.gemwallet.android.features.wallets.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.features.assets.model.IconUrl
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.ui.components.Badge
import com.gemwallet.android.ui.components.ListItem
import com.gemwallet.android.ui.components.ListItemTitle
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
            ""
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
    icon: IconUrl,
    isCurrent: Boolean,
    type: WalletType,
    modifier: Modifier = Modifier,
    onEdit: ((String) -> Unit)? = null,
) {
    ListItem(
        modifier = modifier.heightIn(72.dp),
        iconUrl = icon.ifEmpty { "android.resource://com.gemwallet.android/drawable/multicoin_wallet" },
        supportIcon = if (type == WalletType.view) {
            "android.resource://com.gemwallet.android/drawable/watch_badge"
        } else null,
        trailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer16()
                if (isCurrent) {
                    Icon(
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = "checked",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                if (onEdit != null) {
                    Spacer8()
                    IconButton(onClick = { onEdit(id) }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "edit",
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    ) {
        ListItemTitle(
            title = name,
            subtitle = typeLabel,
            titleBudge = if (type == WalletType.view) {
                { Badge(stringResource(id = R.string.wallets_watch).uppercase()) }
            } else {
                null
            }
        )
    }
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