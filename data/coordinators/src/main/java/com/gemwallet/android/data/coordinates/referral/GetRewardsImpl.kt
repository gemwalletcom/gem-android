package com.gemwallet.android.data.coordinates.referral

import com.gemwallet.android.application.referral.coordinators.GetRewards
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.domains.referral.values.ReferralError
import com.wallet.core.primitives.Rewards

class GetRewardsImpl(
    private val gemApiClient: GemApiClient,
) : GetRewards {
    override suspend fun getRewards(address: String): Rewards {
        val response = gemApiClient.getRewards(address)
        if (response.code == null) {
            throw ReferralError.NotCreated
        }
        return response
    }
}