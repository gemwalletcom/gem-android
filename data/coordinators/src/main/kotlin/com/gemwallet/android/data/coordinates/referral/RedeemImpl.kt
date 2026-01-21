package com.gemwallet.android.data.coordinates.referral

import com.gemwallet.android.application.GetAuthPayload
import com.gemwallet.android.application.referral.coordinators.Redeem
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.tokens.TokensRepository
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.data.services.gemapi.models.ResponseError
import com.gemwallet.android.domains.referral.values.ReferralError
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
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.HttpException

class RedeemImpl(
    private val sessionRepository: SessionRepository,
    private val gemApiClient: GemApiClient,
    private val getAuthPayload: GetAuthPayload,
    private val tokensRepository: TokensRepository,
    private val assetsRepository: AssetsRepository,
) : Redeem {

    override suspend fun redeem(wallet: Wallet, rewards: Rewards, option: RewardRedemptionOption): RedemptionResult {
        val account = wallet.getAccount(Chain.referralChain) ?: throw ReferralError.BadWallet
        val authPayload = getAuthPayload.getAuthPayload(wallet, account.chain)
        if (rewards.points < option.points) {
            throw ReferralError.InsufficientPoints
        }
        return try {
            val result = gemApiClient.redeem(
                address = account.address,
                request = AuthenticatedRequest(
                    auth = authPayload,
                    data = RedemptionRequest(option.id)
                )
            )
            sessionRepository.session().firstOrNull()?.let {
                val assetId = option.asset?.id ?: return@let
                val account = it.wallet.getAccount(assetId.chain) ?: return@let
                tokensRepository.search(assetId)
                assetsRepository.switchVisibility(it.wallet.id, account, assetId, true)
            }
            result
        } catch (err: HttpException) {
            val body = err.response()?.errorBody()?.string() ?: throw ReferralError.NetworkError
            val errorBody = jsonEncoder.decodeFromString<ResponseError>(body)
            throw ReferralError.OperationError(errorBody.error.message)
        } catch (err: Throwable) {
            throw err
        }

    }
}