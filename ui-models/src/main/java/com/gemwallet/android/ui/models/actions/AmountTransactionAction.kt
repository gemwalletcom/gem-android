package com.gemwallet.android.ui.models.actions

import com.gemwallet.android.model.AmountParams

interface AmountTransactionAction {
    operator fun invoke(amountParams: AmountParams)
}