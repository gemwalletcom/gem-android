package com.gemwallet.android.application

interface PasswordStore {
    fun createPassword(walletId: String): String
    fun removePassword(walletId: String): Boolean
    fun getPassword(walletId: String): String
    fun putPassword(walletId: String, password: String)
}