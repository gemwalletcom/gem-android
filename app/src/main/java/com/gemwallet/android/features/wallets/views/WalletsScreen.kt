package com.gemwallet.android.features.wallets.views

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.R
import com.gemwallet.android.ext.getAddressEllipsisText
import com.gemwallet.android.features.wallets.components.WalletItem
import com.gemwallet.android.features.wallets.viewmodels.WalletItemUIState
import com.gemwallet.android.features.wallets.viewmodels.WalletsViewModel
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.DropDownContextItem
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.theme.Spacer8
import com.gemwallet.android.ui.theme.padding16
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var latestLifecycleEvent by remember { mutableStateOf(Lifecycle.Event.ON_ANY) }
    var deleteWalletId by remember { mutableStateOf("") }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            latestLifecycleEvent = event
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    if (latestLifecycleEvent == Lifecycle.Event.ON_RESUME) {
        LaunchedEffect(latestLifecycleEvent) {
            viewModel.refresh()
        }
    }
    UI(
        currentWalletId = uiState.currentWalletId,
        wallets = uiState.wallets,
        onCreate = onCreateWallet,
        onImport = onImportWallet,
        onEdit = onEditWallet,
        onSelectWallet = {
            viewModel.handleSelectWallet(it)
            onSelectWallet()
        },
        onDeleteWallet = {
            deleteWalletId = it
        },
        onCancel = onCancel,
    )

    if (deleteWalletId.isNotEmpty()) {
        AlertDialog(
            text = {
                Text(
                    text = stringResource(
                        id = R.string.common_delete_confirmation,
                        uiState.wallets.firstOrNull{ it.id == deleteWalletId}?.name ?: "" ),
                )
            },
            onDismissRequest = { deleteWalletId = "" },
            confirmButton = {
                TextButton(
                    onClick = {
                        val walletId = deleteWalletId
                        deleteWalletId = ""
                        viewModel.handleDeleteWallet(walletId = walletId, onBoard)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = stringResource(id = R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deleteWalletId = "" },
                ) {
                    Text(text = stringResource(id = R.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun UI(
    currentWalletId: String,
    wallets: List<WalletItemUIState>,
    onCreate: () -> Unit,
    onImport: () -> Unit,
    onEdit: (String) -> Unit,
    onSelectWallet: (String) -> Unit,
    onDeleteWallet: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var longPressedWallet by remember {
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
            items(items = wallets, key = { it.id }) { wallet ->
                DropDownContextItem(
                    isExpanded = longPressedWallet == wallet.id,
                    imeCompensate = true,
                    onDismiss = { longPressedWallet = "" },
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
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.common_wallet)) },
                            trailingIcon = { Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "wallet_config"
                            )},
                            onClick = {
                                onEdit(longPressedWallet)
                                longPressedWallet = ""
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.common_delete),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = "wallet_config"
                                )
                            },
                            onClick = {
                                onDeleteWallet(wallet.id)
                                longPressedWallet = ""
                            }
                        )
                    },
                    onLongClick = { longPressedWallet = wallet.id }
                ) { onSelectWallet(wallet.id) }
            }
        }
    }
}

@Composable
private fun WalletsActions(
    onCreate: () -> Unit,
    onImport: () -> Unit,
) {
    Container {
        Column {
            WalletsAction(
                text = R.string.wallet_create_new_wallet,
                Icons.Default.Add,
                onClick = onCreate,
            )
            HorizontalDivider(modifier = Modifier.padding(start = 58.dp), thickness = 0.4.dp)
            WalletsAction(
                text = R.string.wallet_import_existing_wallet,
                Icons.Default.ArrowDownward,
                onClick = onImport,
            )
        }
    }
}

@Composable
private fun WalletsAction(
    @StringRes text: Int,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(padding16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.padding(4.dp),
            imageVector = icon,
            contentDescription = icon.name,
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Spacer8()
        Text(
            text = stringResource(id = text),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W400),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview
@Composable
fun PreviewWalletScreen() {
    MaterialTheme {
        Box {
            UI(
                wallets = listOf(
                    WalletItemUIState(
                        "1", "Foo wallet #1", WalletType.view, typeLabel = "view"),
                    WalletItemUIState(
                        "2", "Foo wallet #2", WalletType.view, typeLabel = "view"),
                    WalletItemUIState(
                        "3", "Foo wallet #3", WalletType.multicoin, typeLabel = "view"),
                    WalletItemUIState(
                        "4", "Foo wallet #4", WalletType.multicoin, typeLabel = "view"),
                    WalletItemUIState(
                        "5", "Foo wallet #5", WalletType.view, typeLabel = "view"),
                ),
                currentWalletId = "1",
                onEdit = {},
                onCreate = {},
                onImport = {},
                onSelectWallet = {},
                onDeleteWallet = {},
                onCancel = {},
            )
        }
    }
}