package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.proto.Aptos

class AptosSignClient(
    private val chain: Chain,
) : SignClient {
    override suspend fun signTransfer(params: SignerParams, privateKey: ByteArray): ByteArray {
        val coinType = WCChainTypeProxy().invoke(chain)
        val metadata = params.info as AptosSignerPreloader.Info
        val fee = (metadata.fee() as? GasFee) ?: throw Exception("Fee error")
        val signInput = Aptos.SigningInput.newBuilder().apply {
            this.chainId = 1
            this.transfer = Aptos.TransferMessage.newBuilder().apply {
                this.to = params.input.destination()?.address
                this.amount = params.finalAmount.toLong()
            }.build()
            this.expirationTimestampSecs = 3664390082
            this.gasUnitPrice = fee.maxGasPrice.toLong()
            this.maxGasAmount = fee.limit.toLong()
            this.sequenceNumber = metadata.sequence
            this.sender = params.owner
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val output = AnySigner.sign(signInput, coinType, Aptos.SigningOutput.parser())
        return output.json.toByteArray()
    }

    override fun maintainChain(): Chain = chain
}