package com.gemwallet.android.features.wallet.presents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.wallet.viewmodels.WalletUIState
import com.gemwallet.android.features.wallet.viewmodels.WalletViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.image.getIconUrl
import com.gemwallet.android.ui.components.screen.FatalStateScene
import com.gemwallet.android.ui.components.screen.Scene
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType

@Composable
fun WalletScreen(
    onAuthRequest: (() -> Unit) -> Unit,
    onPhraseShow: (String) -> Unit,
    onBoard: () -> Unit,
    onCancel: () -> Unit,
) {
    val viewModel: WalletViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val wallet by viewModel.wallet.collectAsStateWithLifecycle()

    when (state) {
        is WalletUIState.Success -> {
            Wallet(
                wallet = wallet,
                onAuthRequest = onAuthRequest,
                onWalletName = viewModel::setWalletName,
                onPhraseShow = { wallet?.id?.let { onPhraseShow(it) } },
                onDelete = { viewModel.delete(onBoard, onCancel) },
                onCancel = onCancel,
            )
        }
        is WalletUIState.Fatal -> {
            FatalStateScene(
                title = stringResource(id = R.string.common_wallet),
                message = (state as WalletUIState.Fatal).message,
                onCancel = onCancel,
            )
        }
        else -> onCancel()
    }
}

@Composable
private fun Wallet(
    wallet: Wallet?,
    onAuthRequest: (() -> Unit) -> Unit,
    onPhraseShow: () -> Unit,
    onWalletName: (String) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
) {
    wallet ?: return
    var isShowDelete by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboard.current.nativeClipboard

    var walletName by remember(wallet.name) {
        mutableStateOf(wallet.name)
    }
    Scene(
        title = stringResource(id = R.string.common_wallet),
        onClose = onCancel
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Container {
                OutlinedTextField(
                    modifier = Modifier.padding(padding16).fillMaxWidth(),
                    label = { Text(text = stringResource(id = R.string.wallet_name)) },
                    value = walletName,
                    onValueChange = {
                        onWalletName(it)
                        walletName = it
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                    )
                )
            }
            val actions = mutableListOf<CellEntity<String>>()
            when (wallet.type) {
                WalletType.multicoin,
                WalletType.private_key,
                WalletType.single -> actions.add(
                    CellEntity(
                        label = stringResource(
                            id = R.string.common_show,
                            if (wallet.type == WalletType.private_key)
                                stringResource(R.string.common_private_key)
                            else
                                stringResource(id = R.string.common_secret_phrase)
                        ),
                        data = "",
                        action = { onAuthRequest(onPhraseShow) }
                    )
                )
                WalletType.view -> Unit
            }
            when (wallet.type) {
                WalletType.multicoin -> Unit
                WalletType.single,
                WalletType.private_key,
                WalletType.view -> {
                    val account = wallet.accounts.firstOrNull() ?: return@Scene

                    actions.add(
                        CellEntity(
                            label = stringResource(id = R.string.common_address),
                            data = account.address,
                            trailingIcon = account.chain.getIconUrl(),
                            dropDownActions = { callback ->
                                DropdownMenuItem(
                                    text = { Text( text = stringResource(id = R.string.wallet_copy_address)) },
                                    trailingIcon = { Icon(Icons.Default.ContentCopy, "copy") },
                                    onClick = {
                                        callback()
                                        clipboardManager.setPlainText(account.address)
                                    },
                                )
                            },
                        )
                    )
                }
            }

            Table(items = actions)

            Spacer16()
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding16),
                colors = ButtonDefaults.buttonColors().copy(containerColor = MaterialTheme.colorScheme.error),
                onClick = { isShowDelete = true },
            ) {
                Text(text = stringResource(id = R.string.common_delete))
            }
        }
    }

    if (isShowDelete) {
        ConfirmWalletDeleteDialog(
            walletName = walletName,
            onConfirm = {
                isShowDelete = false
                onDelete()
            }
        ) { isShowDelete = false }
    }
}