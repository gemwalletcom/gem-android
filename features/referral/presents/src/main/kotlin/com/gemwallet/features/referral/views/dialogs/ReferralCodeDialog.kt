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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.GemTextField
import com.gemwallet.android.ui.components.filters.FormDialog
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16

@Composable
fun ReferralCodeDialog(
    referralCode: String?,
    onCode: (String, (Exception?) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    var code by remember(referralCode) { mutableStateOf(referralCode ?: "") }
    var showError by remember { mutableStateOf<Exception?>(null) }
    var showProgress by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val dismissDialog: () -> Unit = {
        onDismiss()
        code = ""
    }
    val doneAction: () -> Unit = {
        showProgress = true

        onCode(code) {
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
        ) {
            if (showProgress) {
                CircularProgressIndicator16()
            } else {
                Text(stringResource(R.string.common_done))
            }
        }
    }
    FormDialog(
        title = stringResource(R.string.rewards_referral_code),
        onDismiss = dismissDialog,
        doneAction = done,
    ) {
        GemTextField(
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth(),
            label = stringResource(id = R.string.rewards_referral_code),
            value = code,
            onValueChange = {
                code = it
            },
            keyboardActions = KeyboardActions(onDone = { doneAction() }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            singleLine = true,
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