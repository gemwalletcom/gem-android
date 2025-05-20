package com.gemwallet.android.ui.models.actions

import com.gemwallet.android.model.ConfirmParams

fun interface ConfirmTransactionAction {
    operator fun invoke(confirmParams: ConfirmParams)
}