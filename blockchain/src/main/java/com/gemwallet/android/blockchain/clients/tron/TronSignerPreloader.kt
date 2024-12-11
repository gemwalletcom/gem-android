package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class TronSignerPreloader(
    private val chain: Chain,
    private val rpcClient: TronRpcClient,
) : NativeTransferPreloader, TokenTransferPreloader {
    val feeCalculator = TronFeeCalculator(chain, rpcClient)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        return preloadTransfer(params)
    }

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams {
        return preloadTransfer(params)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private suspend fun preloadTransfer(params: ConfirmParams.TransferParams): SignerParams = withContext(Dispatchers.IO) {
        val feeJob = async { feeCalculator.calculate(params) }
        val nowBlockJob = async { rpcClient.nowBlock() }

        val fee = feeJob.await()
        val nowBlock = nowBlockJob.await().getOrThrow()

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
    ) : ChainSignData {
        override fun fee(speed: TxSpeed): Fee = fee
    }
}