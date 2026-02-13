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
import com.gemwallet.android.domains.wallet.aggregates.WalletDetailsAggregate
import com.gemwallet.android.features.wallet.presents.components.ShowSecretDataProperty
import com.gemwallet.android.features.wallet.presents.components.WalletAddress
import com.gemwallet.android.features.wallet.presents.dialogs.ConfirmWalletDeleteDialog
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.GemTextField
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.defaultPadding

@Composable
internal fun WalletScene(
    wallet: WalletDetailsAggregate?,
    onWalletName: (String) -> Unit,
    onPhraseShow: (String) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
) {
    wallet ?: return
    var showDeleteDialog by remember { mutableStateOf(false) }

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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GemTextField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(id = R.string.wallet_name),
                value = walletName,
                onValueChange = {
                    onWalletName(it)
                    walletName = it
                },
                singleLine = true,
            )
            ShowSecretDataProperty(
                walletId = wallet.id,
                walletType = wallet.type,
                onClick = onPhraseShow,
            )
            WalletAddress(wallet.addresses)

            Spacer16()

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultPadding(),
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.error),
                onClick = { showDeleteDialog = true },
            ) {
                Text(text = stringResource(id = R.string.common_delete))
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmWalletDeleteDialog(
            walletName = walletName,
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            }
        ) { showDeleteDialog = false }
    }
}