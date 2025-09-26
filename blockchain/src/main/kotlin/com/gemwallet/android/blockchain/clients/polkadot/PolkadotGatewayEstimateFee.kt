package com.gemwallet.android.blockchain.clients.polkadot

import com.gemwallet.android.math.append0x
import com.gemwallet.android.math.toHexString
import com.google.protobuf.ByteString
import uniffi.gemstone.Chain
import uniffi.gemstone.GemGatewayEstimateFee
import uniffi.gemstone.GemTransactionLoadFee
import uniffi.gemstone.GemTransactionLoadInput
import uniffi.gemstone.GemTransactionLoadMetadata
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.PrivateKey
import wallet.core.jni.proto.Polkadot
import java.math.BigInteger

class PolkadotGatewayEstimateFee : GemGatewayEstimateFee {
    override suspend fun getFee(
        chain: Chain,
        input: GemTransactionLoadInput
    ): GemTransactionLoadFee? = null

    override suspend fun getFeeData(
        chain: Chain,
        input: GemTransactionLoadInput
    ): String? {
        val metadata = (input.metadata as? GemTransactionLoadMetadata.Polkadot)
            ?.toChainData()
            ?: throw IllegalArgumentException("Incorrect metadata: wait polkadot")

        val transactionData = transactionPayload(
            toAdresss = input.destinationAddress,
            value = input.value.toBigInteger(),
            data = metadata,
        )
        return transactionData
    }

    fun transactionPayload(toAdresss: String, value: BigInteger, data: PolkadotChainData): String {
        val input = Polkadot.SigningInput.newBuilder().apply {
            this.genesisHash = ByteString.copyFrom(data.genesisHash.toByteArray())
            this.blockHash = ByteString.copyFrom(data.blockHash.toByteArray())
            this.nonce = data.sequence.toLong()
            this.specVersion = data.specVersion.toInt()
            this.network = CoinType.POLKADOT.ss58Prefix()
            this.transactionVersion = data.transactionVersion.toInt()
            this.privateKey = ByteString.copyFrom(PrivateKey().data())
            this.era = Polkadot.Era.newBuilder().apply {
                this.blockNumber = data.blockNumber.toLong()
                this.period = data.period
            }.build()
            this.balanceCall = Polkadot.Balance.newBuilder().apply {
                transfer = Polkadot.Balance.Transfer.newBuilder().apply {
                    this.toAddress = toAdresss
                    this.value = ByteString.copyFrom(value.toByteArray())
                }.build()
            }.build()
        }.build()

        val output = AnySigner.sign(input, CoinType.POLKADOT, Polkadot.SigningOutput.parser())
        return output.encoded.toByteArray().toHexString().append0x()
    }
}