package com.gemwallet.android.features.wallet

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.AuthRequest
import com.gemwallet.android.MainActivity
import com.gemwallet.android.R
import com.gemwallet.android.features.wallets.components.ConfirmWalletDeleteDialog
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.FatalStateScene
import com.gemwallet.android.ui.components.Table
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.screen.Scene
import com.wallet.core.primitives.WalletType

@Composable
fun WalletScreen(
    walletId: String,
    isPhrase: Boolean,
    onPhraseShow: (String) -> Unit,
    onBoard: () -> Unit,
    onCancel: () -> Unit,
) {
    val viewModel: WalletViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = walletId) {
        viewModel.init(walletId = walletId, isPhrase = isPhrase)
    }

    when (state) {
        is WalletUIState.Success -> {
            Wallet(
                state = state as WalletUIState.Success,
                onWalletName = viewModel::setWalletName,
                onPhraseShow = { onPhraseShow(walletId) },
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
    state: WalletUIState.Success,
    onPhraseShow: () -> Unit,
    onWalletName: (String) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
) {
    var isShowDelete by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    var walletName by remember(state.walletName) {
        mutableStateOf(state.walletName)
    }
    val context = LocalContext.current
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
            when (state.walletType) {
                WalletType.multicoin,
                WalletType.private_key,
                WalletType.single -> actions.add(
                    CellEntity(
                        label = stringResource(
                            id = R.string.common_show,
                            if (state.walletType == WalletType.private_key)
                                stringResource(R.string.common_private_key)
                            else
                                stringResource(id = R.string.common_secret_phrase)
                        ),
                        data = "",
                        action = {
                            MainActivity.requestAuth(context, AuthRequest.Phrase) {
                                onPhraseShow()
                            }
                        }
                    )
                )
                WalletType.view -> Unit
            }
            when (state.walletType) {
                WalletType.multicoin -> Unit
                WalletType.single,
                WalletType.private_key,
                WalletType.view -> actions.add(
                    CellEntity(
                        label = stringResource(id = R.string.common_address),
                        data = state.walletAddress,
                        trailingIcon = state.chainIconUrl,
                        dropDownActions = { callback ->
                            DropdownMenuItem(
                                text = { Text( text = stringResource(id = R.string.wallet_copy_address)) },
                                trailingIcon = { Icon(Icons.Default.ContentCopy, "copy") },
                                onClick = {
                                    callback()
                                    clipboardManager.setText(AnnotatedString(state.walletAddress))
                                },
                            )
                        },
                    )
                )
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

@Preview
@Composable
private fun PreviewWalletSuccess() {
    MaterialTheme {
        Wallet(
            state = WalletUIState.Success(
                walletName = "Foo wallet #1",
                walletType = WalletType.view
            ),
            onPhraseShow = {},
            onWalletName = {},
            onDelete = {},
            onCancel = {},
        )
    }
}