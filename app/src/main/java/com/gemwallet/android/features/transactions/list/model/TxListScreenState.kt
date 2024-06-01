package com.gemwallet.android.features.transactions.list.model

import com.wallet.core.primitives.TransactionExtended

class TxListScreenState (
    val loading: Boolean = false,
    val transactions: List<TransactionExtended> = emptyList(),
)