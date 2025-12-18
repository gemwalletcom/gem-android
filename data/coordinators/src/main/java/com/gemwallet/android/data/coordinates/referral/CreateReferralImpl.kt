package com.gemwallet.android.data.coordinates.referral

import com.gemwallet.android.application.GetAuthPayload
import com.gemwallet.android.application.referral.coordinators.CreateReferral
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.data.services.gemapi.models.ResponseError
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.referralChain
import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.AuthenticatedRequest
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ReferralCode
import com.wallet.core.primitives.Rewards
import com.wallet.core.primitives.Wallet
import retrofit2.HttpException

class CreateReferralImpl(
    private val gemApiClient: GemApiClient,
    private val getAuthPayload: GetAuthPayload
) : CreateReferral {


    override suspend fun createReferral(code: String, wallet: Wallet): Rewards {
        val account = wallet.getAccount(Chain.referralChain) ?: throw ReferralError.BadWallet
        val authPayload = getAuthPayload.getAuthPayload(wallet, account.chain)
        return try {
            gemApiClient.createReferral(
                AuthenticatedRequest(
                    auth = authPayload,
                    data = ReferralCode(
                        code = code
                    )
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