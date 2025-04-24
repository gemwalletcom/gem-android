package com.gemwallet.android.blockchain.clients

sealed class BlockchainError : Exception() {
    object AccountNotActive : BlockchainError()
}