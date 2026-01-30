package com.gemwallet.android.application.referral.coordinators

import com.wallet.core.primitives.Wallet

interface UseReferralCode {
    suspend fun useReferralCode(code: String, wallet: Wallet, deviceId: String): Boolean
}
