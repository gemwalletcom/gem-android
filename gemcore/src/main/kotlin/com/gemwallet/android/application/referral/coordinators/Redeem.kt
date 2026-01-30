package com.gemwallet.android.application.referral.coordinators

import com.wallet.core.primitives.RedemptionResult
import com.wallet.core.primitives.RewardRedemptionOption
import com.wallet.core.primitives.Rewards
import com.wallet.core.primitives.Wallet

interface Redeem {
    suspend fun redeem(wallet: Wallet, rewards: Rewards, option: RewardRedemptionOption, deviceId: String): RedemptionResult
}
