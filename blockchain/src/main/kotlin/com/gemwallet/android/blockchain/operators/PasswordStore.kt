package com.gemwallet.android.blockchain.operators

interface PasswordStore {
    fun createPassword(walletId: String): String
    fun removePassword(walletId: String): Boolean
    fun getPassword(walletId: String): String
}