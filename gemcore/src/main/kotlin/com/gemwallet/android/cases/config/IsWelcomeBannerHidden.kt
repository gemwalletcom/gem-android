package com.gemwallet.android.cases.config

import kotlinx.coroutines.flow.Flow

interface IsWelcomeBannerHidden {
    fun isWelcomeBannerHidden(walletId: String): Flow<Boolean>
}