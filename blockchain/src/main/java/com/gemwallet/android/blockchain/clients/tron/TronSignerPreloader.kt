package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerInputInfo
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class TronSignerPreloader(
    private val rpcClient: TronRpcClient,
) : SignerPreload {
    override suspend fun invoke(owner: Account, params: ConfirmParams): Result<SignerParams> = withContext(Dispatchers.IO) {
        val feeJob = async {
            try {
                TronFee().invoke(
                    rpcClient = rpcClient,
                    account = owner,
                    recipientAddress = params.destination()?.address!!,
                    value = params.amount,
                    contractAddress = params.assetId.tokenId,
                    type = params.assetId.type(),
                )
            } catch (err: Throwable) {
                null
            }
        }
        val nowBlockJob = async { rpcClient.nowBlock() }

        val fee = feeJob.await() ?: return@withContext Result.failure(Exception("Fee calculation error"))
        val nowBlock = nowBlockJob.await()

        nowBlock.mapCatching {
            SignerParams(
                input = params,
                owner = owner.address,
                info = Info(
                    number = it.block_header.raw_data.number,
                    version = it.block_header.raw_data.version,
                    txTrieRoot = it.block_header.raw_data.txTrieRoot,
                    witnessAddress = it.block_header.raw_data.witness_address,
                    parentHash = it.block_header.raw_data.parentHash,
                    timestamp = it.block_header.raw_data.timestamp,
                    fee = fee,
                )
            )
        }
    }

    override fun maintainChain(): Chain = Chain.Tron

    data class Info(
        val number: Long,
        val version: Long,
        val txTrieRoot: String,
        val witnessAddress: String,
        val parentHash: String,
        val timestamp: Long,
        val fee: Fee,
    ) : SignerInputInfo {
        override fun fee(): Fee = fee
    }
}