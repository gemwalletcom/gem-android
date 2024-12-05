package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.proto.Aptos

class AptosSignClient(
    private val chain: Chain,
) : SignClient {
    override suspend fun signTransfer(params: SignerParams, txSpeed: TxSpeed, privateKey: ByteArray): ByteArray {
        val coinType = WCChainTypeProxy().invoke(chain)
        val metadata = params.chainData as AptosSignerPreloader.AptosChainData
        val fee = metadata.gasGee()
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
            this.sender = params.input.from.address
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val output = AnySigner.sign(signInput, coinType, Aptos.SigningOutput.parser())
        if (output.errorMessage.isNullOrEmpty()) {
            return output.json.toByteArray()
        } else {
            throw Exception(output.errorMessage)
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}