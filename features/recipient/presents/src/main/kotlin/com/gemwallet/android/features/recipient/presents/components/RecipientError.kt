package com.gemwallet.android.features.recipient.presents.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.features.recipient.viewmodel.models.RecipientError
import com.gemwallet.android.ui.R

@Composable
fun recipientErrorString(error: RecipientError): String = when (error) {
    RecipientError.None -> ""
    RecipientError.IncorrectAddress -> stringResource(id = R.string.errors_invalid_address_name)
}