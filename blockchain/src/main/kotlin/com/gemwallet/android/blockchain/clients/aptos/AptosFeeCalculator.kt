package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.aptos.services.AptosFeeService
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.wallet.core.blockchain.aptos.AptosSignature
import com.wallet.core.blockchain.aptos.AptosTransaction
import com.wallet.core.blockchain.aptos.AptosTransactionPayload
import com.wallet.core.blockchain.aptos.AptosTransactionSimulation
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.math.BigInteger

internal class AptosFeeCalculator(
    private val chain: Chain,
    private val feeRpcClient: AptosFeeService,
) {
    private val maxGasAmount = BigInteger.valueOf(1_500)

    suspend fun calculate(params: ConfirmParams, sequence: Long): List<Fee> = withContext(Dispatchers.IO) {
        val feePrice = feeRpcClient.feePrice().getOrThrow()

        FeePriority.entries.map {
            async(Dispatchers.IO) {
                val price = when (it) {
                    FeePriority.Slow -> feePrice.gas_estimate
                    FeePriority.Normal -> feePrice.prioritized_gas_estimate
                    FeePriority.Fast -> feePrice.prioritized_gas_estimate * 2
                }.toBigInteger()

                val gasLimit = when (params) {
                    is ConfirmParams.TransferParams -> {
                        val recipient = params.destination().address
                        when (params.asset.id.type()) {
                            AssetSubtype.NATIVE -> simulateTransactions(
                                sender = params.from.address,
                                recipient = recipient,
                                sequence = sequence,
                                value = params.amount,
                                gasPrice = price,
                                maxGasAmount = maxGasAmount
                            ).gas_used.toBigInteger()

                            AssetSubtype.TOKEN -> maxGasAmount
                        }
                    }
                    is ConfirmParams.SwapParams -> maxGasAmount
                    else -> throw IllegalArgumentException("not supported operation")
                }

                GasFee(
                    feeAssetId = AssetId(chain),
                    priority = it,
                    maxGasPrice = price,
                    limit = gasLimit,
                )
            }
        }.awaitAll()
    }

    private suspend fun simulateTransactions(
        sender: String,
        recipient: String,
        sequence: Long,
        value: BigInteger,
        gasPrice: BigInteger,
        maxGasAmount: BigInteger
    ): AptosTransaction {
        val transaction = AptosTransactionSimulation(
            expiration_timestamp_secs = (System.currentTimeMillis() / 1_000 + 1_000_000).toString(),
            gas_unit_price = gasPrice.toString(),
            max_gas_amount = maxGasAmount.toString(),
            payload = AptosTransactionPayload(
                arguments = listOf(
                    recipient,
                    value.toString(),
                ),
                function = "0x1::aptos_account::transfer",
                type = "entry_function_payload",
                type_arguments = emptyList(),
            ),
            sender = sender,
            sequence_number = sequence.toString(),
            signature = AptosSignature(
                type = "no_account_signature",
                public_key = null,
                signature = null,
            ),
        )
        val response = feeRpcClient.simulate(transaction)
        val simulated = response.getOrNull()?.firstOrNull()
        return simulated ?: throw Exception("No aptos transaction")
    }
}