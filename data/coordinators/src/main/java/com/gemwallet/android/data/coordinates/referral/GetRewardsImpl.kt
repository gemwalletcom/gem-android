package com.gemwallet.android.data.coordinates.referral

import com.gemwallet.android.application.referral.coordinators.GetRewards
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.wallet.core.primitives.Rewards

class GetRewardsImpl(
    private val gemApiClient: GemApiClient,
) : GetRewards {
    override suspend fun getRewards(address: String): Rewards {
        return gemApiClient.getRewards(address)
    }
}