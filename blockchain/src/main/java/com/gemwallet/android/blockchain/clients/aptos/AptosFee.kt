package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.TxSpeed
import com.google.gson.Gson
import com.wallet.core.blockchain.aptos.models.AptosErrorCode
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigInteger

internal class AptosFee {
    suspend operator fun invoke(
        chain: Chain,
        destination: String,
        rpcClient: AptosRpcClient,
    ): Fee = withContext(Dispatchers.IO) {
        val gasPriceJob =
            async { rpcClient.feePrice().getOrThrow().prioritized_gas_estimate.toBigInteger() }
        val isNewJob = async {
            val result = rpcClient.accounts(destination).getOrThrow()
            if (result.sequence_number != null) {
                false
            } else {
                if (result.error_code == AptosErrorCode.account_not_found.string) {
                    true
                } else {
                    throw Exception(result.message)
                }
            }
        }
        val (gasPrice, isNew) = Pair(gasPriceJob.await(), isNewJob.await())
        GasFee(
            feeAssetId = AssetId(chain),
            speed = TxSpeed.Normal,
            maxGasPrice = gasPrice,
            limit = BigInteger.valueOf(if (isNew) 676 else 9).multiply(BigInteger("2"))
        )
    }
}