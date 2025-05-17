package com.gemwallet.android.features.recipient.presents.views

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gemwallet.android.features.recipient.viewmodel.AddressChainViewModel
import com.gemwallet.android.ui.components.clipboard.getPlainText
import com.gemwallet.android.ui.components.designsystem.space4
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.NameRecord

@Composable
fun ColumnScope.AddressChainField(
    chain: Chain?,
    value: String,
    label: String,
    onValueChange: (String, NameRecord?) -> Unit,
    error: String = "",
    editable: Boolean = true,
    searchName: Boolean = true,
    onQrScanner: (() -> Unit)? = null,
    viewModel: AddressChainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboard.current.nativeClipboard

    LaunchedEffect(key1 = value) {
        viewModel.onNameRecord(chain, value)
    }

    LaunchedEffect(key1 = uiState.nameRecord?.address) {
        onValueChange(uiState.nameRecord?.name ?: value, uiState.nameRecord)
    }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (it.hasFocus) keyboardController?.show() else keyboardController?.hide()
            },
        value = value,
        singleLine = true,
        readOnly = !editable,
        label = { Text(label) },
        onValueChange = { newValue ->
            if (searchName) {
                viewModel.onInput(newValue, chain)
            }
            onValueChange(newValue, uiState.nameRecord)
        },
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator16()
                    Spacer(modifier = Modifier.size(8.dp))
                }
                if (uiState.isResolve) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Name is resolved",
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                if (uiState.isFail) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Error,
                        contentDescription = "Name is fail",
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                TransferTextFieldActions(
                    value = value,
                    paste = { onValueChange(clipboardManager.getPlainText() ?: "", uiState.nameRecord) },
                    qrScanner = onQrScanner,
                    onClean = {
                        onValueChange("", null)
                        viewModel.onInput("", null)
                    }
                )
            }
        }
    )
    if (error.isNotEmpty()) {
        Spacer(modifier = Modifier.size(space4))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
fun TransferTextFieldActions(
    value: String,
    paste: (() -> Unit)? = null,
    qrScanner: (() -> Unit)? = null,
    onClean: () -> Unit
) {
    if (value.isNotEmpty()) {
        IconButton(onClick = onClean) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "clear",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        return
    }
    Row {
        if (paste != null) {
            IconButton(onClick = paste) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "paste",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (qrScanner != null) {
            IconButton(onClick = qrScanner) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "scan_address",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}