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
import com.gemwallet.android.domains.wallet.aggregates.WalletDataAggregate
import com.gemwallet.android.features.wallet.presents.dialogs.ConfirmWalletDeleteDialog
import com.gemwallet.features.wallets.viewmodels.WalletsViewModel
import com.wallet.core.primitives.Chain
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
    val pinnedWallets by viewModel.pinned.collectAsStateWithLifecycle()
    val unpinnedWallets by viewModel.unpinned.collectAsStateWithLifecycle()

    var deleteWalletId by remember { mutableStateOf("") }

    WalletsScene(
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
                    object : WalletDataAggregate {
                        override val id: String = "1"
                        override val name: String = "Foo wallet #1"
                        override val type: WalletType = WalletType.View
                        override val isCurrent: Boolean = true
                        override val isPinned: Boolean = false
                        override val walletChain: Chain = Chain.Ethereum
                        override val walletAddress: String = "0xsdlkgjdlkfglkdjfg"
                    },
                    object : WalletDataAggregate {
                        override val id: String = "1"
                        override val name: String = "Foo wallet #3"
                        override val type: WalletType = WalletType.Multicoin
                        override val isCurrent: Boolean = false
                        override val isPinned: Boolean = false
                        override val walletChain: Chain = Chain.Ethereum
                        override val walletAddress: String = "0xsdlkgjdlkfglkdjfg"
                    },
                    object : WalletDataAggregate {
                        override val id: String = "1"
                        override val name: String = "Foo wallet #2"
                        override val type: WalletType = WalletType.PrivateKey
                        override val isCurrent: Boolean = false
                        override val isPinned: Boolean = false
                        override val walletChain: Chain = Chain.Bitcoin
                        override val walletAddress: String = "0xsdlkgjdlkfglkdjfg"
                    },
                ),
                pinnedWallets = listOf(

                    object : WalletDataAggregate {
                        override val id: String = "1"
                        override val name: String = "Foo wallet #4"
                        override val type: WalletType = WalletType.Multicoin
                        override val isCurrent: Boolean = true
                        override val isPinned: Boolean = true
                        override val walletChain: Chain = Chain.Bitcoin
                        override val walletAddress: String = "0xsdlkgjdlkfglkdjfg"
                    },
                ),
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