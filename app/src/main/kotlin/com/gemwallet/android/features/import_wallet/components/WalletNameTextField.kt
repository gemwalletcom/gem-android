package com.gemwallet.android.features.import_wallet.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gemwallet.android.ui.theme.space4

@Composable
fun WalletNameTextField(
    value: String = "",
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    error: String = "",
    singleLine: Boolean = true,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = placeholder) },
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth(),
        )
        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.size(space4))
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }
    }
}