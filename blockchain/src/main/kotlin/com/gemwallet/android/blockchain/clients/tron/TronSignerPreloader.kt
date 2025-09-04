package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.StakeTransactionPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.blockchain.tron.TronAccount
import com.wallet.core.blockchain.tron.TronAccountUsage
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigInteger

class TronSignerPreloader(
    private val chain: Chain,
    private val client: TronRpcClient,
) : NativeTransferPreloader, TokenTransferPreloader, StakeTransactionPreloader, SwapTransactionPreloader  {
    val feeCalculator = TronFeeCalculator(chain, client)

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
            votes.filter { it.value > 0 }.mapValues { it.value.toULong() }
        }
    }

    override suspend fun preloadSwap(params: ConfirmParams.SwapParams): SignerParams {
        TODO("Not yet implemented")
    }

    private suspend fun preload(
        params: ConfirmParams,
        feeCalc: suspend (TronAccount?, TronAccountUsage?) -> Fee,
        votes: suspend (TronAccount?) -> Map<String, ULong>,
    ): SignerParams = withContext(Dispatchers.IO) {
        val getAccountUsage = async { client.getAccountUsage(params.from.address) }
        val getAccount = async { client.getAccount(params.from.address, true) }
        val nowBlockJob = async { client.nowBlock() }

        val nowBlock = nowBlockJob.await().getOrThrow()
        val account = getAccount.await()
        val accountUsage = getAccountUsage.await()

        val fee = feeCalc(account, accountUsage)

        SignerParams(
            input = params,
            chainData = TronChainData(
                blockNumber = nowBlock.block_header.raw_data.number.toULong(),
                blockVersion = nowBlock.block_header.raw_data.version.toULong(),
                txTrieRoot = nowBlock.block_header.raw_data.txTrieRoot,
                witnessAddress = nowBlock.block_header.raw_data.witness_address,
                parentHash = nowBlock.block_header.raw_data.parentHash,
                blockTimestamp = nowBlock.block_header.raw_data.timestamp.toULong(),
                votes = votes(account)
            ),
            fee = listOf(fee),
        )
    }

}