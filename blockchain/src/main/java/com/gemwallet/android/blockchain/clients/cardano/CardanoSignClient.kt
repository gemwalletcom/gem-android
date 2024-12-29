package com.gemwallet.android.blockchain.clients.cardano

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Cardano

class CardanoSignClient(
    private val chain: Chain
) : SignClient {

    override suspend fun signTransfer(
        params: SignerParams,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): ByteArray {
        val chainData = (params.chainData as? CardanoSignerPreloaderClient.CardanoChainData)
            ?: throw IllegalArgumentException()
        val signingInput = Cardano.SigningInput.newBuilder().apply {
            this.addPrivateKey(ByteString.copyFrom(privateKey))
            this.transferMessage = Cardano.Transfer.newBuilder().apply {
                this.toAddress = params.input.destination()?.address!!
                this.changeAddress = params.input.from.address
                this.amount = params.input.amount.toLong()
                this.useMaxAmount = params.input.isMax()
            }.build()
            this.ttl = 190000000
            this.addAllUtxos(
                chainData.utxos.map { utxo ->
                    Cardano.TxInput.newBuilder().apply {
                        this.outPoint = Cardano.OutPoint.newBuilder().apply {
                            this.txHash = ByteString.copyFrom(utxo.transaction_id.decodeHex())
                            this.outputIndex = utxo.vout.toLong()
                        }.build()
                        this.address = utxo.address
                        this.amount = utxo.value.toLong()
                    }.build()
                }
            )
        }.build()
        val output = AnySigner.sign(signingInput, CoinType.CARDANO, Cardano.SigningOutput.parser())
        if (!output.errorMessage.isNullOrEmpty()) {
            throw Exception(output.errorMessage)
        }
        return output.encoded.toByteArray()
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}