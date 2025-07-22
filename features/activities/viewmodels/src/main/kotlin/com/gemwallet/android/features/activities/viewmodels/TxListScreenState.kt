package com.gemwallet.android.features.activities.viewmodels

import com.gemwallet.android.model.TransactionExtended

class TxListScreenState (
    val loading: Boolean = false,
    val transactions: List<TransactionExtended> = emptyList(),
)