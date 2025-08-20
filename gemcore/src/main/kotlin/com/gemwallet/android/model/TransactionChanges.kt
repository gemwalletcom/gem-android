package com.gemwallet.android.model

import com.wallet.core.primitives.TransactionState
import java.math.BigInteger

data class TransactionChanges(
    val state: TransactionState,
    val fee: BigInteger? = null,
    val hashChanges: HashChanges? = null,
)

data class HashChanges(
    val old: String,
    val new: String,
)
