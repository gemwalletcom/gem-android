package com.gemwallet.android.cases.update

interface CheckForUpdateCase {
    suspend fun checkForUpdate(): String?
}