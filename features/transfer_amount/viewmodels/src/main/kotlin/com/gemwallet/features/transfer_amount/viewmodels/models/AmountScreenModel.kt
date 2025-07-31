package com.gemwallet.features.transfer_amount.viewmodels.models

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.TransactionType

class AmountScreenModel(
    val txType: TransactionType,
    val asset: Asset,
)