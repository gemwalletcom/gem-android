package com.gemwallet.android.cases.config

interface HideWelcomeBanner {
    suspend fun hideWelcomeBanner(walletId: String)
}