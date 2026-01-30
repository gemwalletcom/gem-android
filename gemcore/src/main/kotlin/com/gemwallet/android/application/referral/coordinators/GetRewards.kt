package com.gemwallet.android.application.referral.coordinators

import com.wallet.core.primitives.Rewards

interface GetRewards {
    suspend fun getRewards(walletId: String): Rewards
}
