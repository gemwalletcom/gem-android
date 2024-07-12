package com.gemwallet.android.features.recipient.models

class RecipientScreenModel(
    val addressError: RecipientFormError = RecipientFormError.None,
    val memoError: RecipientFormError = RecipientFormError.None,
)