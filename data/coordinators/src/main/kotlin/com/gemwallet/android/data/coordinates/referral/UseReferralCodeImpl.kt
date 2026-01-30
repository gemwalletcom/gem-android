package com.gemwallet.android.data.coordinates.referral

import com.gemwallet.android.application.GetAuthPayload
import com.gemwallet.android.application.referral.coordinators.UseReferralCode
import com.gemwallet.android.cases.device.GetDeviceId
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.data.services.gemapi.models.ResponseError
import com.gemwallet.android.domains.referral.values.ReferralError
import com.gemwallet.android.ext.getAccount
import com.gemwallet.android.ext.referralChain
import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.AuthenticatedRequest
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ReferralCode
import com.wallet.core.primitives.Wallet
import retrofit2.HttpException

class UseReferralCodeImpl(
    private val gemApiClient: GemApiClient,
    private val getDeviceId: GetDeviceId,
    private val getAuthPayload: GetAuthPayload,
) : UseReferralCode {


    override suspend fun useReferralCode(code: String, wallet: Wallet): Boolean {
        val account = wallet.getAccount(Chain.referralChain) ?: throw ReferralError.BadWallet
        val auth = getAuthPayload.getAuthPayload(wallet, account.chain)
        return try {
            gemApiClient.useReferralCode(
                deviceId = getDeviceId.getDeviceId(),
                walletId = wallet.id,
                body = AuthenticatedRequest(
                    auth = auth,
                    data = ReferralCode(code)
                )
            )
            true
        } catch (err: HttpException) {
            val body = err.response()?.errorBody()?.string() ?: throw ReferralError.NetworkError
            val errorBody = jsonEncoder.decodeFromString<ResponseError>(body)
            throw ReferralError.OperationError(errorBody.error.message)
        } catch (err: Throwable) {
            throw err
        }
    }
}
