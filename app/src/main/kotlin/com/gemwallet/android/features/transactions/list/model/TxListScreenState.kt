package com.gemwallet.android.features.transactions.list.model

import com.gemwallet.android.model.TransactionExtended


class TxListScreenState (
    val loading: Boolean = false,
    val transactions: List<TransactionExtended> = emptyList(),
)