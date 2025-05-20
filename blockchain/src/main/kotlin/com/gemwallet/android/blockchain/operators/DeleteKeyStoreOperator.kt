package com.gemwallet.android.blockchain.operators

interface DeleteKeyStoreOperator {
    operator fun invoke(walletId: String): Boolean
}