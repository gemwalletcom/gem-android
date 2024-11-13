package com.gemwallet.android.ui.models.actions

import com.gemwallet.android.model.AmountParams

fun interface AmountTransactionAction {
    operator fun invoke(amountParams: AmountParams)
}