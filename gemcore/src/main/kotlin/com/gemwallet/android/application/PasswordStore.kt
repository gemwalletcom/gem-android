package com.gemwallet.android.application

interface PasswordStore {
    fun createPassword(key: String): String
    fun removePassword(key: String): Boolean
    fun getPassword(key: String): String
    fun putPassword(key: String, password: String)
}