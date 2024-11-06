package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.StakeClient
import com.gemwallet.android.ext.asset
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import wallet.core.jni.EthereumAbiValue
import java.math.BigInteger

class SmartchainStakeClient(
    private val evmRpcClient: EvmRpcClient,
    private val stakeHub: StakeHub = StakeHub(),
) : StakeClient {
    override suspend fun getValidators(apr: Double): List<DelegationValidator> {
        val limit = getMaxElectedValidators()
        val data = stakeHub.encodeValidatorsCall(0, limit)
        val result = evmRpcClient.callString(StakeHub.reader, data) ?: return emptyList()
        val validators = stakeHub.decodeValidatorsReturn(result)
        return validators
    }

    override suspend fun getStakeDelegations(address: String, apr: Double): List<DelegationBase> = withContext(Dispatchers.IO) {
        val limit = getMaxElectedValidators()
        val getDelegationCall = async { getDelegations(address, limit) }
        val getUndelegationsCall = async { getUndelegations(address, limit) }
        val delegations = getDelegationCall.await()
        val undelegations = getUndelegationsCall.await()
        delegations + undelegations
    }

    suspend fun getBalance(address: String, availableBalance: AssetBalance?): AssetBalance? = withContext(Dispatchers.IO) {
        if (availableBalance == null) {
            return@withContext null
        }
        val limit = getMaxElectedValidators()
        val getDelegationCall = async { getDelegations(address, limit) }
        val getUndelegationsCall = async { getUndelegations(address, limit) }
        val delegations = getDelegationCall.await()
        val undelegations = getUndelegationsCall.await()
        val staked = delegations.sumBalances()
        val pending = undelegations.sumBalances()

        AssetBalance.create(
            asset = Chain.SmartChain.asset(),
            available = availableBalance.balance.available,
            staked = staked.toString(),
            pending = pending.toString()
        )
    }

    override fun maintainChain(): Chain = Chain.SmartChain

    private suspend fun getDelegations(address: String, limit: Int): List<DelegationBase> {
        val data = evmRpcClient.callString(StakeHub.reader, stakeHub.encodeDelegationsCall(address, limit))
            ?: return emptyList()
        return stakeHub.decodeDelegationsResult(data)
    }

    private suspend fun getUndelegations(address: String, limit: Int): List<DelegationBase> {
        val data = evmRpcClient.callString(StakeHub.reader, stakeHub.encodeUndelegationsCall(address, limit))
            ?: return emptyList()
        return stakeHub.decodeUnelegationsResult(data)
    }

    private fun List<DelegationBase>.sumBalances(): BigInteger = filter { it.state == DelegationState.Active }
        .map { try { it.balance.toBigInteger() } catch (_: Throwable) { BigInteger.ZERO} }
        .fold(BigInteger.ZERO) {
                acc, value -> acc + value
        }

    private suspend fun getMaxElectedValidators(): Int {
        val result = evmRpcClient.callString(StakeHub.address, stakeHub.encodeMaxElectedValidators())
            ?: throw IllegalStateException("Unable to get validators")
        return EthereumAbiValue.decodeUInt256(result.decodeHex()).toUShort().toInt()
    }
}