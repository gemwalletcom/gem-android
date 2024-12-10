package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.StakeClient
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmCallService
import com.gemwallet.android.blockchain.clients.ethereum.services.callString
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
        val limit = getMaxElectedValidators()
        val getDelegationCall = async { getDelegations(address, limit) }
        val getUndelegationsCall = async { getUndelegations(address, limit) }
        val delegations = getDelegationCall.await()
        val undelegations = getUndelegationsCall.await()
        delegations + undelegations
    }

    suspend fun getBalance(address: String, availableValue: String?): AssetBalance? = withContext(Dispatchers.IO) {
        availableValue ?: return@withContext null
        val limit = getMaxElectedValidators()
        val getDelegationCall = async { getDelegations(address, limit) }
        val getUndelegationsCall = async { getUndelegations(address, limit) }
        val delegations = getDelegationCall.await()
        val undelegations = getUndelegationsCall.await()
        val staked = delegations.filter { it.state == DelegationState.Active }.sumBalances()
        val pending = undelegations.filter {
            it.state == DelegationState.Undelegating || it.state == DelegationState.AwaitingWithdrawal
        }.sumBalances()

        AssetBalance.create(
            asset = Chain.SmartChain.asset(),
            available = availableValue,
            staked = staked.toString(),
            pending = pending.toString()
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private suspend fun getDelegations(address: String, limit: Int): List<DelegationBase> {
        val dataRequest = StakeHub.encodeDelegationsCall(address, limit)
        val data = callService.callString(StakeHub.reader, dataRequest) ?: return emptyList()
        return StakeHub.decodeDelegationsResult(data)
    }

    private suspend fun getUndelegations(address: String, limit: Int): List<DelegationBase> {
        val data = callService.callString(StakeHub.reader, StakeHub.encodeUndelegationsCall(address, limit))
            ?: return emptyList()
        return StakeHub.decodeUnelegationsResult(data)
    }

    private suspend fun getMaxElectedValidators(): Int {
        val result = callService.callString(StakeHub.address, StakeHub.encodeMaxElectedValidators())
            ?: throw IllegalStateException("Unable to get validators")
        return EthereumAbiValue.decodeUInt256(result.decodeHex()).toUShort().toInt()
    }

    private fun List<DelegationBase>.sumBalances(): BigInteger =
        map { it.balance.toBigIntegerOrNull() ?: BigInteger.ZERO }
            .fold(BigInteger.ZERO) {acc, value -> acc + value }
}