package com.gemwallet.android.application

interface SecurityStore<T> {
    suspend fun getValue(key: T): String

    suspend fun putValue(key: T, value: String)
}