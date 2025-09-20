package com.gemwallet.android.blockchain.model

sealed class TransactionStatusError(message: String? = null) : Exception(message)

object ServiceUnavailable : TransactionStatusError()