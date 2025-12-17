package com.gemwallet.android.data.coordinates.referral

import com.gemwallet.android.application.GetAuthPayload
import com.gemwallet.android.application.referral.coordinators.Redeem
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.referralChain
import com.wallet.core.primitives.AuthenticatedRequest
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.RedemptionRequest
import com.wallet.core.primitives.RedemptionResult
import com.wallet.core.primitives.Wallet

class RedeemImpl(
    private val gemApiClient: GemApiClient,
    private val getAuthPayload: GetAuthPayload
) : Redeem {

    override suspend fun redeem(wallet: Wallet): RedemptionResult {
        val account = wallet.getAccount(Chain.referralChain) ?: throw ReferralError.BadWallet
        val authPayload = getAuthPayload.getAuthPayload(wallet, account.chain)
        return gemApiClient.redeem(
            address = account.address,
            request = AuthenticatedRequest(
                auth = authPayload,
                data = RedemptionRequest("")
            )
        )
    }
}