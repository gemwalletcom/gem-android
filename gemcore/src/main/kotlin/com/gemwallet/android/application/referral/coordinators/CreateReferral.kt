package com.gemwallet.android.application.referral.coordinators

import com.wallet.core.primitives.Rewards
import com.wallet.core.primitives.Wallet

interface CreateReferral {
    suspend fun createReferral(code: String, wallet: Wallet, deviceId: String): Rewards
}
