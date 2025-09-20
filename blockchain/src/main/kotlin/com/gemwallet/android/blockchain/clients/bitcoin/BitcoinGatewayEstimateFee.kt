package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.blockchain.services.mapper.toUtxo
import com.gemwallet.android.ext.toChain
import com.wallet.core.primitives.UTXO
import uniffi.gemstone.Chain
import uniffi.gemstone.GemFeeOptions
import uniffi.gemstone.GemGasPriceType
import uniffi.gemstone.GemGatewayEstimateFee
import uniffi.gemstone.GemTransactionLoadFee
import uniffi.gemstone.GemTransactionLoadInput
import uniffi.gemstone.GemTransactionLoadMetadata
import wallet.core.java.AnySigner
import wallet.core.jni.BitcoinSigHashType
import wallet.core.jni.proto.Bitcoin
import wallet.core.jni.proto.Common
import java.math.BigInteger

class BitcoinGatewayEstimateFee : GemGatewayEstimateFee {
    override suspend fun getFee(
        chain: Chain,
        input: GemTransactionLoadInput
    ): GemTransactionLoadFee? {
        val chain = chain.toChain() ?: throw IllegalArgumentException("Incorrect chain")

        val utxos = (input.metadata as? GemTransactionLoadMetadata.Bitcoin)?.utxos?.toUtxo()
            ?: throw IllegalArgumentException("Incorrect UTXO")

        val bytePrice = (input.gasPrice as? GemGasPriceType.Regular)?.gasPrice?.toLongOrNull()
            ?: throw IllegalArgumentException("Incorrect Byte Price")
        val amount = input.value.toLongOrNull() ?: return null

        val destinationAddress = input.destinationAddress
        val senderAddress = input.senderAddress

        val fee = calcFee(
            chain = chain,
            senderAddress = senderAddress,
            destinationAddress = destinationAddress,
            amount = amount,
            bytePrice = bytePrice,
            utxos = utxos
        )

        return GemTransactionLoadFee(
            fee = fee.toString(),
            gasPriceType = input.gasPrice,
            gasLimit = "1",
            options = GemFeeOptions(emptyMap())
        )
    }

    override suspend fun getFeeData(
        chain: Chain,
        input: GemTransactionLoadInput
    ): String? {
        return null
    }

    private fun calcFee(
        chain: com.wallet.core.primitives.Chain,
        senderAddress: String,
        destinationAddress: String,
        amount: Long,
        bytePrice: Long,
        utxos: List<UTXO>,
    ): BigInteger {
        val coinType = WCChainTypeProxy().invoke(chain)
        val total = utxos.map { it.value.toLong() }.fold(0L) { x, y -> x + y }
        if (total == 0L) {
            return BigInteger.ZERO // empty balance
        }
        val input = Bitcoin.SigningInput.newBuilder().apply {
            this.hashType = BitcoinSigHashType.ALL.value()
            this.byteFee = bytePrice
            this.amount = amount
            this.useMaxAmount = total == amount
            this.coinType = coinType.value()
            this.toAddress = destinationAddress
            this.changeAddress = senderAddress
            this.addAllUtxo(utxos.getUtxoTransactions(senderAddress, coinType))
        }.build()

        val plan = AnySigner.plan(input, coinType, Bitcoin.TransactionPlan.parser())
        when (plan.error) {
            Common.SigningError.OK -> { /* continue */ }
            Common.SigningError.Error_not_enough_utxos,
            Common.SigningError.Error_dust_amount_requested,
            Common.SigningError.Error_missing_input_utxos -> throw IllegalStateException("Dust Error: $bytePrice")
            else -> throw IllegalStateException(plan.error.name)
        }

        val selectedUtxos: MutableList<UTXO> = mutableListOf()
        for (raw in plan.utxosList) {
            input.utxoList?.indexOfFirst { it == raw }?.let {
                selectedUtxos.add(utxos[it])
            }
        }
        if (utxos.isNotEmpty() && selectedUtxos.isEmpty() && amount != total && amount <= total) {
            return calcFee(chain, senderAddress, destinationAddress, total, bytePrice, utxos)
        }
        return BigInteger.valueOf(plan.fee/* / bytePrice*/) //
    }
}