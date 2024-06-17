package com.gemwallet.android.interactors

interface SyncOperator {
    suspend operator fun invoke()
}