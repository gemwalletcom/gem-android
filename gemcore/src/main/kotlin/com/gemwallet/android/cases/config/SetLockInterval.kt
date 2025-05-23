package com.gemwallet.android.cases.config

interface SetLockInterval {
    suspend fun setLockInterval(minutes: Int)
}