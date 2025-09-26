package com.gemwallet.android.blockchain.clients.cardano

import com.gemwallet.android.blockchain.services.mapper.toUtxo
import com.gemwallet.android.math.decodeHex
import com.google.protobuf.ByteString
import com.wallet.core.primitives.UTXO
import uniffi.gemstone.Chain
import uniffi.gemstone.GemFeeOptions
import uniffi.gemstone.GemGatewayEstimateFee
import uniffi.gemstone.GemTransactionLoadFee
import uniffi.gemstone.GemTransactionLoadInput
import uniffi.gemstone.GemTransactionLoadMetadata
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Cardano
import java.math.BigInteger

class CardanoGatewayEstimateFee : GemGatewayEstimateFee {
    override suspend fun getFee(
        chain: Chain,
        input: GemTransactionLoadInput
    ): GemTransactionLoadFee? {
        val utxos = (input.metadata as? GemTransactionLoadMetadata.Cardano)?.utxos?.toUtxo()
            ?: throw IllegalArgumentException("Incorrect UTXO")

        val amount = input.value.toLongOrNull() ?: return null

        val destination = input.destinationAddress
        val from = input.senderAddress

        val fee = calcFee(
            from = from,
            to = destination, amount,
            useMaxAmount = input.isMaxValue,
            utxos
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
        from: String,
        to: String,
        amount: Long,
        useMaxAmount: Boolean,
        utxos: List<UTXO>,
    ): BigInteger {
        val signingInput = Cardano.SigningInput.newBuilder().apply {
            this.addAllUtxos(
                utxos.map { utxo ->
                    Cardano.TxInput.newBuilder().apply {
                        this.address = utxo.address
                        this.amount = utxo.value.toLong()
                        this.outPoint = Cardano.OutPoint.newBuilder().apply {
                            this.txHash = ByteString.copyFrom(utxo.transaction_id.decodeHex())
                            this.outputIndex = utxo.vout.toLong()
                        }.build()
                    }.build()
                }
            )
            this.transferMessage = Cardano.Transfer.newBuilder().apply {
                this.toAddress = to
                this.changeAddress = from
                this.amount = amount
                this.useMaxAmount = useMaxAmount
            }.build()
        }.build()
        val plan: Cardano.TransactionPlan = AnySigner.plan(signingInput, CoinType.CARDANO, Cardano.TransactionPlan.parser())
        return plan.fee.toBigInteger()
    }
}