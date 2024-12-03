package com.gemwallet.android.blockchain.clients.near

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigInteger

class NearSignerPreloader(
    private val chain: Chain,
    private val rpcClient: NearRpcClient,
) : NativeTransferPreloader {

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams = withContext(Dispatchers.IO) {
        val getAccountJob = async { rpcClient.accountAccessKey(params.from.address) }
        val blockJob = async { rpcClient.latestBlock() }

        val account = getAccountJob.await()
        val block = blockJob.await()

        val fee = BigInteger("900000000000000000000")

        SignerParams(
            input = params,
            chainData = NearChainData(
                sequence = account.nonce + 1L,
                block = block.header.hash,
                fee = Fee(
                    feeAssetId = AssetId(chain),
                    speed = TxSpeed.Normal,
                    amount = fee,
                )
            )
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    data class NearChainData(
        val block: String,
        val sequence: Long,
        val fee: Fee,
    ) : ChainSignData {
        override fun fee(speed: TxSpeed): Fee = fee
    }
}