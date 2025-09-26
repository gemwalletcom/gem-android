package com.gemwallet.android.blockchain.clients.polkadot

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Polkadot
import java.math.BigInteger

class PolkadotSignClient(
    private val chain: Chain
) : SignClient {

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = (chainData as? PolkadotChainData) ?: throw Exception("incomplete data")
        val input = Polkadot.SigningInput.newBuilder().apply {
            this.genesisHash = ByteString.copyFrom(chainData.genesisHash.decodeHex())
            this.blockHash = ByteString.copyFrom(chainData.blockHash.decodeHex())
            this.nonce = chainData.sequence.toLong()
            this.specVersion = chainData.specVersion.toInt()
            this.network = CoinType.POLKADOT.ss58Prefix()
            this.transactionVersion = chainData.transactionVersion.toInt()
            this.privateKey = ByteString.copyFrom(privateKey)
            this.era = Polkadot.Era.newBuilder().apply {
                this.blockNumber = chainData.blockNumber.toLong()
                this.period = chainData.period
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
}