package com.gemwallet.android.features.wallet.presents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.GemTextField
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.defaultPadding
import com.wallet.core.primitives.Wallet

@Composable
internal fun WalletScene(
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
        actions = {
            TextButton(
                onClick = onCancel,
                colors = ButtonDefaults.textButtonColors()
                    .copy(contentColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Text(stringResource(R.string.common_done).uppercase())
            }
        },
        onClose = onCancel
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
        ) {
            GemTextField(
                modifier = Modifier.Companion.fillMaxWidth(),
                label = stringResource(id = R.string.wallet_name),
                value = walletName,
                onValueChange = {
                    onWalletName(it)
                    walletName = it
                },
                singleLine = true,
            )
            ShowSecretData(wallet, onAuthRequest, onPhraseShow)
            WalletAddress(wallet)

            Spacer16()
            Button(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .defaultPadding(),
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.error),
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