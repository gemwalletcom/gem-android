package com.gemwallet.android.blockchain.clients.algorand

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.Base64
import wallet.core.jni.proto.Algorand

class AlgorandSignClient(
    private val chain: Chain
) : SignClient {
    
    override suspend fun signTransaction(
        params: SignerParams,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = (params.chainData as? AlgorandSignPreloadClient.AlgorandChainData)
            ?: throw Exception("bad params")
        val input = Algorand.SigningInput.newBuilder().apply {
            this.genesisId = chainData.chainId
            this.genesisHash = ByteString.copyFrom(Base64.decode(chainData.block))
            if (params.input.memo() != null) {
                this.note = params.input.memo()?.let {
                    ByteString.copyFrom(it.toByteArray())
                }
            }
            this.firstRound = chainData.sequence.toLong()
            this.lastRound = chainData.sequence.toLong() + 1000
            this.fee = chainData.fee().amount.toLong()
            this.privateKey = ByteString.copyFrom(privateKey)
            this.transfer = Algorand.Transfer.newBuilder().apply {
                this.toAddress = params.input.destination()!!.address
                this.amount = params.finalAmount.toLong()
            }.build()
        }.build()
        val output = AnySigner.sign(input, WCChainTypeProxy().invoke(chain), Algorand.SigningOutput.parser())
        if (!output.errorMessage.isNullOrEmpty()) {
            throw Exception(output.errorMessage)
        }
        return listOf(output.encoded.toByteArray())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}