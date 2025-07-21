package com.gemwallet.android.features.recipient.presents.views

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType

fun LazyListScope.walletsDestination(
    toChain: Chain,
    items: List<Wallet>,
    onSelect: (Account) -> Unit,
) {
    walletsSection(
        header = R.string.common_pinned,
        toChain = toChain,
        items = items,
        onSelect = onSelect,
        isPinned = true,
        WalletType.multicoin, WalletType.private_key, WalletType.single, WalletType.view,
    )

    walletsSection(
        header = R.string.transfer_recipient_my_wallets,
        toChain = toChain,
        items = items,
        onSelect = onSelect,
        isPinned = false,
        WalletType.multicoin, WalletType.private_key, WalletType.single
    )
    walletsSection(
        header = R.string.transfer_recipient_view_wallets,
        toChain = toChain,
        items = items,
        onSelect = onSelect,
        isPinned = false,
        WalletType.view
    )
}

private fun LazyListScope.walletsSection(
    @StringRes header: Int,
    toChain: Chain,
    items: List<Wallet>,
    onSelect: (Account) -> Unit,
    isPinned: Boolean = false,
    vararg types: WalletType
) {
    items.filter { types.contains(it.type) && it.getAccount(toChain) != null && it.isPinned == isPinned }
        .takeIf { it.isNotEmpty() }
        ?.let { wallets ->
            item {
                Spacer8()
                SubheaderItem(stringResource(header))
            }
            items(wallets) { wallet ->
                WalletRecipient(wallet) { onSelect(wallet.getAccount(toChain) ?: return@WalletRecipient) }
            }
        }
}

@Composable
private fun WalletRecipient(
    wallet: Wallet,
    onClick: () -> Unit
) {
    PropertyItem(
        modifier = Modifier.clickable(onClick = onClick),
        title = { PropertyTitleText(wallet.name) },
        data = { PropertyDataText("", badge = { DataBadgeChevron() }) }
    )
}