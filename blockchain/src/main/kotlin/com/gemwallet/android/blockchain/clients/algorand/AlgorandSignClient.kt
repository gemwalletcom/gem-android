package com.gemwallet.android.blockchain.clients.algorand

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.Base64
import wallet.core.jni.proto.Algorand
import java.math.BigInteger

class AlgorandSignClient(
    private val chain: Chain
) : SignClient {
    
    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = (chainData as? AlgorandChainData)
            ?: throw Exception("bad params")
        val input = Algorand.SigningInput.newBuilder().apply {
            this.genesisId = chainData.chainId
            this.genesisHash = ByteString.copyFrom(Base64.decode(chainData.block))
            if (params.memo() != null) {
                this.note = params.memo()?.let { ByteString.copyFrom(it.toByteArray()) }
            }
            this.firstRound = chainData.sequence.toLong()
            this.lastRound = chainData.sequence.toLong() + 1000
            this.fee = fee.amount.toLong()
            this.privateKey = ByteString.copyFrom(privateKey)
            this.transfer = Algorand.Transfer.newBuilder().apply {
                this.toAddress = params.destination().address
                this.amount = finalAmount.toLong()
            }.build()
        }.build()
        val output = AnySigner.sign(input, WCChainTypeProxy().invoke(chain), Algorand.SigningOutput.parser())
        if (!output.errorMessage.isNullOrEmpty()) {
            throw Exception(output.errorMessage)
        }
        return listOf(output.encoded.toByteArray().toHexString("").toByteArray())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}