package com.gemwallet.android.features.recipient.presents.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.gemwallet.android.features.recipient.viewmodel.models.RecipientError
import com.gemwallet.android.ui.components.GemTextField
import com.gemwallet.android.ui.components.clipboard.getPlainText
import com.gemwallet.android.ui.theme.space4

@Composable
fun MemoTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    error: RecipientError = RecipientError.None,
    onQrScanner: (() -> Unit)? = null,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboard.current.nativeClipboard
    GemTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (it.hasFocus) keyboardController?.show() else keyboardController?.hide()
            },
        value = value,
        singleLine = true,
        label = label,
        onValueChange = onValueChange,
        trailing = {
            TransferTextFieldActions(
                value = value,
                paste = { onValueChange(clipboardManager.getPlainText() ?: "") },
                qrScanner = onQrScanner,
                onClean = {
                    onValueChange("")
                }
            )
        }
    )
    if (error == RecipientError.None) {
        Spacer(modifier = Modifier.size(space4))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = recipientErrorString(error),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}