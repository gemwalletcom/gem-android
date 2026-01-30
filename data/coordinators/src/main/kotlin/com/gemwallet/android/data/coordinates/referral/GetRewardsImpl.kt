package com.gemwallet.android.data.coordinates.referral

import com.gemwallet.android.application.referral.coordinators.GetRewards
import com.gemwallet.android.cases.device.GetDeviceId
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.domains.referral.values.ReferralError
import com.wallet.core.primitives.Rewards

class GetRewardsImpl(
    private val gemApiClient: GemApiClient,
    private val getDeviceId: GetDeviceId,
) : GetRewards {
    override suspend fun getRewards(walletId: String): Rewards {
        val response = gemApiClient.getRewards(getDeviceId.getDeviceId(), walletId)
        if (response.code == null) {
            throw ReferralError.NotCreated
        }
        return response
    }
}
