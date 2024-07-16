package com.gemwallet.android.features.confirm.models

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.TransactionType

class AmountScreenModel(
    val txType: TransactionType,
    val asset: Asset,
)