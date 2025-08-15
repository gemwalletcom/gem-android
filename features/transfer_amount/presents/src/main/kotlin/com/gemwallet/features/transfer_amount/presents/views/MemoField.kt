package com.gemwallet.features.transfer_amount.presents.views

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.gemwallet.android.ui.components.fields.TransferTextFieldActions
import com.gemwallet.android.ui.theme.space4
import com.gemwallet.features.transfer_amount.viewmodels.models.AmountError

@Composable
fun MemoTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    error: AmountError = AmountError.None,
    onQrScanner: (() -> Unit)? = null,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboard.current.nativeClipboard
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (it.hasFocus) keyboardController?.show() else keyboardController?.hide()
            },
        value = value,
        singleLine = true,
        label = { Text(label) },
        onValueChange = onValueChange,
        trailingIcon = {
            TransferTextFieldActions(
                value = value,
                paste = { onValueChange(clipboardManager.primaryClip?.getItemAt(0)?.text?.toString() ?: "") },
                qrScanner = onQrScanner,
                onClean = {
                    onValueChange("")
                }
            )
        }
    )
    if (error != AmountError.None) {
        Spacer(modifier = Modifier.size(space4))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = amountErrorString(error),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}