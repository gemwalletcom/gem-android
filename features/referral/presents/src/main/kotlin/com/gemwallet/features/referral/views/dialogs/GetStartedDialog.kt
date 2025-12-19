package com.gemwallet.features.referral.views.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.GemTextField
import com.gemwallet.android.ui.components.filters.FormDialog
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16
import com.gemwallet.android.ui.theme.Spacer8

@Composable
internal fun GetStartedDialog(
    onUsername: (String, (Exception?) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf<Exception?>(null) }
    var showProgress by remember { mutableStateOf(false) }

    val dismissDialog: () -> Unit = {
        onDismiss()
        username = ""
    }
    val doneAction: () -> Unit = {
        showProgress = true
        onUsername(username) {
            showProgress = false
            if (it == null) {
                dismissDialog()
            } else {
                showError = it
            }
        }
    }
    val done: @Composable () -> Unit = {
        TextButton(
            onClick = doneAction,
            enabled = !showProgress
        ) {
            if (showProgress) {
                CircularProgressIndicator16()
            } else {
                Text(stringResource(R.string.common_done))
            }
        }
    }
    FormDialog(
        title = stringResource(R.string.rewards_create_referral_code_title),
        onDismiss = dismissDialog,
        doneAction = done,
    ) {
        GemTextField(
            modifier = Modifier.Companion.fillMaxWidth(),
            label = stringResource(id = R.string.rewards_username),
            value = username,
            onValueChange = {
                username = it
            },
            singleLine = true,
            keyboardActions = KeyboardActions(onDone = { doneAction() }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Companion.Done),
        )
        Spacer8()
        Text(
            modifier = Modifier.Companion.fillMaxWidth(),
            text = stringResource(R.string.rewards_create_referral_code_info),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Companion.Center
        )
    }

    if (showError != null) {
        AlertDialog(
            onDismissRequest = { showError = null },
            containerColor = MaterialTheme.colorScheme.background,
            confirmButton = {
                Button({ showError = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            text = {
                Text(showError?.message ?: return@AlertDialog)
            }
        )
    }
}