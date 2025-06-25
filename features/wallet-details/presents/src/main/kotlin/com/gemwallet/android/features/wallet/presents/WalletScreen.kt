package com.gemwallet.android.features.wallet.presents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.wallet.viewmodels.WalletUIState
import com.gemwallet.android.features.wallet.viewmodels.WalletViewModel
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.Container
import com.gemwallet.android.ui.components.clipboard.setPlainText
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.designsystem.padding8
import com.gemwallet.android.ui.components.list_item.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.PropertyDataText
import com.gemwallet.android.ui.components.list_item.PropertyItem
import com.gemwallet.android.ui.components.list_item.PropertyTitleText
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

    var walletName by remember(wallet.name) {
        mutableStateOf(wallet.name)
    }
    Scene(
        title = stringResource(id = R.string.common_wallet),
        onClose = onCancel
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Container {
                OutlinedTextField(
                    modifier = Modifier
                        .padding(padding16)
                        .fillMaxWidth(),
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
            ShowSecretData(wallet, onAuthRequest, onPhraseShow)
            WalletAddress(wallet)

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

@Composable
private fun WalletAddress(
    wallet: Wallet,
) {
    if (wallet.type == WalletType.multicoin) {
        return
    }
    val account = wallet.accounts.firstOrNull() ?: return
    var isDropDownShow by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboard.current.nativeClipboard
    val context = LocalContext.current
    PropertyItem(
        modifier = Modifier.combinedClickable(
            enabled = true,
            onClick = {},
            onLongClick = { isDropDownShow = true}
        ),
        title = { PropertyTitleText(R.string.common_address) },
        data = { PropertyDataText(modifier = Modifier.weight(1f), text = account.address) },
    )
    Box(modifier = Modifier.fillMaxWidth()) {
        DropdownMenu(
            modifier = Modifier.align(Alignment.BottomEnd),
            expanded = isDropDownShow,
            offset = DpOffset(padding16, padding8),
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = { isDropDownShow = false },
        ) {
            DropdownMenuItem(
                text = { Text( text = stringResource(id = R.string.wallet_copy_address)) },
                trailingIcon = { Icon(Icons.Default.ContentCopy, "copy") },
                onClick = {
                    isDropDownShow = false
                    clipboardManager.setPlainText(context, account.address)
                },
            )
        }
    }
}

@Composable
private fun ShowSecretData(
    wallet: Wallet,
    onAuthRequest: (() -> Unit) -> Unit,
    onPhraseShow: () -> Unit,
) {
    if (wallet.type == WalletType.view) {
        return
    }
    PropertyItem(
        modifier = Modifier.clickable(onClick = { onAuthRequest(onPhraseShow) }),
        title = {
            PropertyTitleText(
                text = stringResource(
                    id = R.string.common_show,
                    if (wallet.type == WalletType.private_key)
                        stringResource(R.string.common_private_key)
                    else
                        stringResource(id = R.string.common_secret_phrase)
                )
            )
        },
        data = { PropertyDataText("", badge = { DataBadgeChevron() })}
    )
}