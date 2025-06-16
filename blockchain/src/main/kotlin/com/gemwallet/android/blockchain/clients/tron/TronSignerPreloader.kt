package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.StakeTransactionPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.blockchain.clients.tron.services.TronAccountsService
import com.gemwallet.android.blockchain.clients.tron.services.TronCallService
import com.gemwallet.android.blockchain.clients.tron.services.TronNodeStatusService
import com.gemwallet.android.blockchain.clients.tron.services.getAccount
import com.gemwallet.android.blockchain.clients.tron.services.getAccountUsage
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.blockchain.tron.models.TronAccount
import com.wallet.core.blockchain.tron.models.TronAccountUsage
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigInteger

class TronSignerPreloader(
    private val chain: Chain,
    private val nodeStatusService: TronNodeStatusService,
    private val accountsService: TronAccountsService,
    callService: TronCallService,
) : NativeTransferPreloader, TokenTransferPreloader, StakeTransactionPreloader, SwapTransactionPreloader  {
    val feeCalculator = TronFeeCalculator(chain, nodeStatusService, callService)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        return preload(
            params, { account, usage ->
                feeCalculator.calculate(params, account, usage)
            }
        ) { emptyMap() }
    }

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams {
        return preload(
            params, { account, usage ->
                feeCalculator.calculate(params, account, usage)
            }
        ) { emptyMap() }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    override suspend fun preloadStake(params: ConfirmParams.Stake): SignerParams = withContext(Dispatchers.IO) {
        preload(
            params = params,
            feeCalc = { account, usage ->
                feeCalculator.calculate(params, account, usage)
            }
        ) { account ->
            val newVote = (params.amount / BigInteger.TEN.pow(chain.asset().decimals)).toLong()
            val votes = (account?.votes ?: emptyList()).groupBy { it.vote_address }.mapValues {
                it.value.fold(0L) { acc, v -> acc + v.vote_count}
            }.toMutableMap()
            when (params) {
                is ConfirmParams.Stake.DelegateParams -> votes[params.validatorId] = (votes[params.validatorId] ?: 0L) + newVote
                is ConfirmParams.Stake.UndelegateParams -> votes[params.validatorId] = (votes[params.validatorId] ?: 0L) - newVote
                is ConfirmParams.Stake.RedelegateParams -> {
                    votes[params.srcValidatorId] = (votes[params.srcValidatorId] ?: 0L) - newVote
                    votes[params.dstValidatorId] = (votes[params.dstValidatorId] ?: 0L) + newVote
                }
                is ConfirmParams.Stake.RewardsParams,
                is ConfirmParams.Stake.WithdrawParams -> {}
            }
            votes.filter { it.value > 0 }
        }
    }

    override suspend fun preloadSwap(params: ConfirmParams.SwapParams): SignerParams {
        TODO("Not yet implemented")
    }

    private suspend fun preload(
        params: ConfirmParams,
        feeCalc: suspend (TronAccount?, TronAccountUsage?) -> Fee,
        votes: suspend (TronAccount?) -> Map<String, Long>,
    ): SignerParams = withContext(Dispatchers.IO) {
        val getAccountUsage = async { accountsService.getAccountUsage(params.from.address) }
        val getAccount = async { accountsService.getAccount(params.from.address, true) }
        val nowBlockJob = async { nodeStatusService.nowBlock() }

        val nowBlock = nowBlockJob.await().getOrThrow()
        val account = getAccount.await()
        val accountUsage = getAccountUsage.await()

        val fee = feeCalc(account, accountUsage)

        SignerParams(
            input = params,
            chainData = TronChainData(
                number = nowBlock.block_header.raw_data.number,
                version = nowBlock.block_header.raw_data.version,
                txTrieRoot = nowBlock.block_header.raw_data.txTrieRoot,
                witnessAddress = nowBlock.block_header.raw_data.witness_address,
                parentHash = nowBlock.block_header.raw_data.parentHash,
                timestamp = nowBlock.block_header.raw_data.timestamp,
                fee = fee,
                votes = votes(account)
            )
        )
    }

    data class TronChainData(
        val number: Long,
        val version: Long,
        val txTrieRoot: String,
        val witnessAddress: String,
        val parentHash: String,
        val timestamp: Long,
        val fee: Fee,
        val votes: Map<String, Long> = emptyMap()
    ) : ChainSignData {
        override fun fee(speed: FeePriority): Fee = fee
    }
}