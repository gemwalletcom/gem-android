package com.gemwallet.android.cases.update

interface SkipVersionCase {
    suspend fun skipVersion(version: String)
}