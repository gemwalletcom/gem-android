package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.StakeClient
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmCallService
import com.gemwallet.android.blockchain.clients.ethereum.services.batch
import com.gemwallet.android.blockchain.clients.ethereum.services.callString
import com.gemwallet.android.blockchain.clients.ethereum.services.createCallRequest
import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wallet.core.jni.EthereumAbiValue
import java.math.BigInteger

class SmartchainStakeClient(
    private val chain: Chain,
    private val callService: EvmCallService,
) : StakeClient {

    override suspend fun getValidators(chain: Chain, apr: Double): List<DelegationValidator> {
        val limit = getMaxElectedValidators()
        val data = StakeHub.encodeValidatorsCall(0, limit)
        val result = callService.callString(StakeHub.reader, data) ?: return emptyList()
        val validators = StakeHub.decodeValidatorsReturn(result)
        return validators
    }

    override suspend fun getStakeDelegations(chain: Chain, address: String, apr: Double): List<DelegationBase> = withContext(Dispatchers.IO) {
        val limit = 128
        val delegationDataRequest = StakeHub.encodeDelegationsCall(address, limit)
        val undelegationDataRequest = StakeHub.encodeUndelegationsCall(address, limit)
        val requests = listOf(
            callService.createCallRequest(StakeHub.reader, delegationDataRequest, "latest"),
            callService.createCallRequest(StakeHub.reader, undelegationDataRequest, "latest"),
        )
        val result = callService.batch(requests)
        val delegations = StakeHub.decodeDelegationsResult(result[0])
        val undelegations = StakeHub.decodeUnelegationsResult(result[1])

        delegations + undelegations
    }

    suspend fun getBalance(address: String): AssetBalance? = withContext(Dispatchers.IO) {
        val delegations = getStakeDelegations(chain, address, 0.0)

        val staked = delegations.filter { it.state == DelegationState.Active }.sumBalances()

        val pending = delegations.filter {
            it.state == DelegationState.Undelegating || it.state == DelegationState.AwaitingWithdrawal
        }.sumBalances()

        AssetBalance.create(
            asset = Chain.SmartChain.asset(),
            staked = staked.toString(),
            pending = pending.toString()
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private suspend fun getMaxElectedValidators(): Int {
        val result = callService.callString(StakeHub.address, StakeHub.encodeMaxElectedValidators())
            ?: throw IllegalStateException("Unable to get validators")
        return EthereumAbiValue.decodeUInt256(result.decodeHex()).toUShort().toInt()
    }

    private fun List<DelegationBase>.sumBalances(): BigInteger =
        map { it.balance.toBigIntegerOrNull() ?: BigInteger.ZERO }
            .fold(BigInteger.ZERO) {acc, value -> acc + value }
}