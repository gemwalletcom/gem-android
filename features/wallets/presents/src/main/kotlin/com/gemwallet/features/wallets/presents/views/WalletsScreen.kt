package com.gemwallet.features.wallets.presents.views

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.wallet.presents.ConfirmWalletDeleteDialog
import com.gemwallet.features.wallets.viewmodels.WalletsViewModel
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType

@Composable
fun WalletsScreen(
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit,
    onEditWallet: (String) -> Unit,
    onSelectWallet: () -> Unit,
    onBoard: () -> Unit,
    onCancel: () -> Unit,
) {
    val viewModel: WalletsViewModel = hiltViewModel()
    val currentWallet by viewModel.currentWallet.collectAsStateWithLifecycle()
    val pinnedWallets by viewModel.pinnedWallets.collectAsStateWithLifecycle()
    val unpinnedWallets by viewModel.unpinnedWallets.collectAsStateWithLifecycle()
    var deleteWalletId by remember { mutableStateOf("") }

    WalletsScene(
        currentWalletId = currentWallet?.id ?: "",
        pinnedWallets = pinnedWallets,
        unpinnedWallets = unpinnedWallets,
        onCreate = onCreateWallet,
        onImport = onImportWallet,
        onEdit = onEditWallet,
        onSelectWallet = {
            viewModel.selectWallet(it)
            onSelectWallet()
        },
        onDeleteWallet = {
            deleteWalletId = it
        },
        onTogglePin = viewModel::togglePin,
        onCancel = onCancel,
    )

    if (deleteWalletId.isNotEmpty()) {
        ConfirmWalletDeleteDialog(
            walletName = (unpinnedWallets + pinnedWallets).firstOrNull{ it.id == deleteWalletId}?.name ?: "",
            onConfirm = {
                val walletId = deleteWalletId
                deleteWalletId = ""
                viewModel.deleteWallet(walletId = walletId, onBoard)
            }
        ) {
            deleteWalletId = ""
        }
    }
}

@Preview
@Composable
fun PreviewWalletScreen() {
    MaterialTheme {
        Box {
            WalletsScene(
                unpinnedWallets = listOf(
                    Wallet(
                        "1", "Foo wallet #1", 1, WalletType.view, emptyList(), 0, false
                    ),
                    Wallet(
                        "2", "Foo wallet #2", 2, WalletType.view, emptyList(), 0, false
                    ),
                    Wallet(
                        "3", "Foo wallet #3", 3, WalletType.multicoin, emptyList(), 1, false
                    ),
                    Wallet(
                        "4", "Foo wallet #4", 4, WalletType.multicoin, emptyList(), 2, false
                    ),
                ),
                pinnedWallets = listOf(
                    Wallet(
                        "5", "Foo wallet #5", 44, WalletType.multicoin, emptyList(), 2, true
                    ),
                ),
                currentWalletId = "1",
                onEdit = {},
                onCreate = {},
                onImport = {},
                onSelectWallet = {},
                onDeleteWallet = {},
                onTogglePin = {},
                onCancel = {},
            )
        }
    }
}