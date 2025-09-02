package com.gemwallet.android.blockchain.clients.polkadot

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.clients.polkadot.models.PolkadotSigningData
import com.gemwallet.android.math.append0x
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.PrivateKey
import wallet.core.jni.proto.Polkadot
import java.math.BigInteger

class PolkadotSignClient(
    private val chain: Chain
) : SignClient {

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = (chainData as? PolkadotSignerPreloaderClient.PolkadotChainData) ?: throw Exception("incomplete data")
        val data = chainData.data
        val input = Polkadot.SigningInput.newBuilder().apply {
            this.genesisHash = ByteString.copyFrom(data.genesisHash.decodeHex())
            this.blockHash = ByteString.copyFrom(data.blockHash.decodeHex())
            this.nonce = chainData.sequence.toLong()
            this.specVersion = data.specVersion.toInt()
            this.network = CoinType.POLKADOT.ss58Prefix()
            this.transactionVersion = data.transactionVersion.toInt()
            this.privateKey = ByteString.copyFrom(privateKey)
            this.era = Polkadot.Era.newBuilder().apply {
                this.blockNumber = data.blockNumber.toLong()
                this.period = data.period
            }.build()
            this.balanceCall = Polkadot.Balance.newBuilder().apply {
                transfer = Polkadot.Balance.Transfer.newBuilder().apply {
                    this.toAddress = params.destination().address
                    this.value = ByteString.copyFrom(finalAmount.toByteArray())
                }.build()
            }.build()
        }.build()
        val output = AnySigner.sign(input, CoinType.POLKADOT, Polkadot.SigningOutput.parser())
        return listOf(output.encoded.toByteArray().toHexString().toByteArray())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    companion object {
        fun transactionPayload(toAdresss: String, value: BigInteger, nonce: Long, data: PolkadotSigningData): String {
            val input = Polkadot.SigningInput.newBuilder().apply {
                this.genesisHash = ByteString.copyFrom(data.genesisHash.toByteArray())
                this.blockHash = ByteString.copyFrom(data.blockHash.toByteArray())
                this.nonce = nonce
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
}