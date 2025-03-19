package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.StakeClient
import com.gemwallet.android.blockchain.clients.tron.services.TronAccountsService
import com.gemwallet.android.blockchain.clients.tron.services.TronStakeService
import com.gemwallet.android.blockchain.clients.tron.services.getAccount
import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.decodeHex
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import wallet.core.jni.Base58
import java.math.BigInteger
import kotlin.math.roundToLong

class TronStakeClient(
    private val chain: Chain,
    private val stakeService: TronStakeService,
    private val accountsService: TronAccountsService,
) : StakeClient {
    override suspend fun getValidators(
        chain: Chain,
        apr: Double
    ): List<DelegationValidator> {
        val validators = stakeService.listwitnesses().getOrNull()?.witnesses
            ?.mapNotNull {
                DelegationValidator(
                    id = addressBase58(it.address) ?: return@mapNotNull null,
                    chain = chain,
                    name = "",
                    isActive = it.isJobs == true,
                    commision = 0.0,
                    apr = apr,
                )
            } ?: emptyList()
        return validators + getSystem()
    }


    override suspend fun getStakeDelegations(
        chain: Chain,
        address: String,
        apr: Double
    ): List<DelegationBase> = withContext(Dispatchers.IO) {
        val getAccount = async { accountsService.getAccount(address, true) }
        val getValidators = async { getValidators(chain, apr = 0.0) }
        val getReward = async { stakeService.getReward(address).getOrNull()?.reward ?: 0L }

        val account = getAccount.await()
        val validators = getValidators.await()
        val reward = getReward.await()

        val pendingDelegations = (account?.unfrozenV2 ?: emptyList()).mapNotNull { unfrozen ->
            val expireTime = unfrozen.unfreeze_expire_time ?: return@mapNotNull null
            val amount = unfrozen.unfreeze_amount ?: return@mapNotNull null
            val completionDate = expireTime
            val state = if (System.currentTimeMillis() < expireTime) DelegationState.Pending else DelegationState.AwaitingWithdrawal
            val balance = BigInteger.valueOf(amount)

            DelegationBase(
                assetId = AssetId(chain),
                state = state,
                balance = balance.toString(),
                shares = "",
                rewards = "0",
                completionDate = completionDate,
                delegationId = completionDate.toString(),
                validatorId = getSystem().id,
            )
        }

        val votes = account?.votes ?: emptyList()
        val delegations = votes.mapNotNull { vote ->
            val validator = validators.firstOrNull { it.id == vote.vote_address } ?: return@mapNotNull null
            val balance = BigInteger.valueOf(vote.vote_count) * BigInteger.TEN.pow(chain.asset().decimals)
            val totalVotes = votes.fold(0L) { acc, item -> acc + item.vote_count }
            val rewards = if (totalVotes <= 0) {
                BigInteger.valueOf(reward)
            } else {
                val proportion = vote.vote_count.toDouble() / totalVotes.toDouble()
                val rewardForVote = reward.toDouble() * proportion
                val roundedRewardForVote = rewardForVote.roundToLong()
                BigInteger.valueOf(roundedRewardForVote)
            }

            DelegationBase(
                assetId = AssetId(chain),
                state = DelegationState.Active,
                balance = balance.toString(),
                shares = "",
                rewards = rewards.toString(),
                completionDate = null,
                delegationId = "${balance}_${reward}".toString(),
                validatorId = validator.id,
            )
        }
        pendingDelegations + delegations
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private fun getSystem() = DelegationValidator(
        id = "unstaking",
        chain = chain,
        name = "Unstaking",
        isActive = true,
        commision = 0.0,
        apr = 0.0,
    )

    private fun addressBase58(hex: String): String? {
        try {
            return Base58.encode(hex.decodeHex())
        } catch (_: Throwable) {
            return null
        }
    }
}