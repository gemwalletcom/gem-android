package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.StakeTransactionPreloader
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class CosmosSignerPreloader(
    private val chain: Chain,
    private val rpcClient: CosmosRpcClient,
) : NativeTransferPreloader, StakeTransactionPreloader {

    private val feeCalculator = CosmosFeeCalculator(chain)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        return preload(params)
    }
    override suspend fun preloadStake(params: ConfirmParams.Stake): SignerParams {
        return preload(params)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private suspend fun preload(params: ConfirmParams) = withContext(Dispatchers.IO) {
        val accountJob = async {
            when (chain) {
                Chain.Injective -> rpcClient.getInjectiveAccountData(params.from.address).getOrNull()?.account?.base_account
                else -> rpcClient.getAccountData(params.from.address).getOrNull()?.account
            }
        }
        val nodeInfoJob = async { rpcClient.getNodeInfo().getOrNull()?.block?.header?.chain_id }
        val fee = feeCalculator.calculate(params.getTxType())

        val (account, nodeInfo) = Pair(
            accountJob.await() ?: throw Exception("Can't get data (account) for sign"),
            nodeInfoJob.await() ?: throw Exception("Can't get data (node info) for sign")
        )
        SignerParams(
            input = params,
            chainData = CosmosChainData(
                chainId = nodeInfo,
                accountNumber = account.account_number.toLong(),
                sequence = account.sequence.toLong(),
                fee = fee,
            )
        )
    }

    data class CosmosChainData(
        val chainId: String,
        val accountNumber: Long,
        val sequence: Long,
        val fee: Fee,
    ) : ChainSignData {
        override fun fee(speed: TxSpeed): Fee = fee
    }
}