package com.gemwallet.android.interactors

interface SyncOperator {
    operator suspend fun invoke(): Result<Boolean>
}