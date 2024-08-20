package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigInteger

internal class AptosFee {
    suspend operator fun invoke(
        chain: Chain,
        ownerAddress: String,
        rpcClient: AptosRpcClient,
    ): Fee = withContext(Dispatchers.IO) {
        val gasPriceJob =
            async { rpcClient.feePrice().getOrThrow().prioritized_gas_estimate.toBigInteger() }
        val sequenceJob = async {
            try {
                rpcClient.accounts(ownerAddress).getOrThrow().sequence_number.toLong()
            } catch (err: Throwable) {
                null
            }
        }
        val (gasPrice, sequence) = Pair(gasPriceJob.await(), sequenceJob.await())
        GasFee(
            feeAssetId = AssetId(chain),
            speed = TxSpeed.Normal,
            maxGasPrice = gasPrice,
            limit = BigInteger.valueOf(if (sequence == null) 676 else 6)
        )
    }
}