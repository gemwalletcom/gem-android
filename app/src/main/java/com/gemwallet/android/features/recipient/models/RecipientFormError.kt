package com.gemwallet.android.features.recipient.models

sealed interface RecipientFormError {
    data object None : RecipientFormError

    data object IncorrectAddress : RecipientFormError
}