package com.gemwallet.android.features.recipient.models

sealed interface RecipientScreenModel {
    data class Idle(
        val addressError: RecipientFormError = RecipientFormError.None,
        val memoError: RecipientFormError = RecipientFormError.None,
    ) : RecipientScreenModel

    data object ScanQr : RecipientScreenModel
}