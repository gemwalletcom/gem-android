package com.gemwallet.android.application.referral.coordinators

import com.wallet.core.primitives.RedemptionResult
import com.wallet.core.primitives.Wallet

interface Redeem {
    suspend fun redeem(wallet: Wallet): RedemptionResult
}