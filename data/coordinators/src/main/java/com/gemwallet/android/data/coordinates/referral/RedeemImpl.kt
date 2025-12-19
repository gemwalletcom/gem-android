package com.gemwallet.android.data.coordinates.referral

import com.gemwallet.android.application.GetAuthPayload
import com.gemwallet.android.domains.referral.values.ReferralError
import com.gemwallet.android.application.referral.coordinators.Redeem
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.data.services.gemapi.models.ResponseError
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.referralChain
import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.AuthenticatedRequest
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.RedemptionRequest
import com.wallet.core.primitives.RedemptionResult
import com.wallet.core.primitives.RewardRedemptionOption
import com.wallet.core.primitives.Rewards
import com.wallet.core.primitives.Wallet
import retrofit2.HttpException

class RedeemImpl(
    private val gemApiClient: GemApiClient,
    private val getAuthPayload: GetAuthPayload
) : Redeem {

    override suspend fun redeem(wallet: Wallet, rewards: Rewards, option: RewardRedemptionOption): RedemptionResult {
        val account = wallet.getAccount(Chain.referralChain) ?: throw ReferralError.BadWallet
        val authPayload = getAuthPayload.getAuthPayload(wallet, account.chain)
        if (rewards.points < option.points) {
            throw ReferralError.InsufficientPoints
        }
        return try {
            gemApiClient.redeem(
                address = account.address,
                request = AuthenticatedRequest(
                    auth = authPayload,
                    data = RedemptionRequest(option.id)
                )
            )
        } catch (err: HttpException) {
            val body = err.response()?.errorBody()?.string() ?: throw ReferralError.NetworkError
            val errorBody = jsonEncoder.decodeFromString<ResponseError>(body)
            throw ReferralError.OperationError(errorBody.error.message)
        } catch (err: Throwable) {
            throw err
        }

    }
}