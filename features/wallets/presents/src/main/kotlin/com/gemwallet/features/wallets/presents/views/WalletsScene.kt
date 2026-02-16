package com.gemwallet.features.wallets.presents.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.domains.wallet.aggregates.WalletDataAggregate
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.features.wallets.presents.views.components.WalletsActions
import com.gemwallet.features.wallets.presents.views.components.wallets

@Composable
internal fun WalletsScene(
    pinnedWallets: List<WalletDataAggregate>,
    unpinnedWallets: List<WalletDataAggregate>,
    onCreate: () -> Unit,
    onImport: () -> Unit,
    onEdit: (String) -> Unit,
    onSelectWallet: (String) -> Unit,
    onDeleteWallet: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val longPressedWallet = remember {
        mutableStateOf("")
    }

    Scene(
        title = stringResource(id = R.string.wallets_title),
        onClose = onCancel,
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                WalletsActions(onCreate = onCreate, onImport = onImport)
            }
            wallets(
                wallets = pinnedWallets,
                longPressedWallet = longPressedWallet,
                onEdit = onEdit,
                onSelectWallet = onSelectWallet,
                onDeleteWallet = onDeleteWallet,
                onTogglePin = onTogglePin,
                isPinned = true,
            )
            wallets(
                wallets = unpinnedWallets,
                longPressedWallet = longPressedWallet,
                onEdit = onEdit,
                onSelectWallet = onSelectWallet,
                onDeleteWallet = onDeleteWallet,
                onTogglePin = onTogglePin,
            )
        }
    }
}