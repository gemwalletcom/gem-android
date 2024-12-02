package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.proto.Ripple

class XrpSignClient(
    private val chain: Chain,
) : SignClient {
    override suspend fun signTransfer(params: SignerParams, txSpeed: TxSpeed, privateKey: ByteArray): ByteArray {
        val metadata = params.info as XrpSignerPreloader.Info
        val signInput = Ripple.SigningInput.newBuilder().apply {
            this.fee = metadata.fee().amount.toLong()
            this.sequence = metadata.sequence
            this.account = params.owner
            this.privateKey = ByteString.copyFrom(privateKey)
            this.opPayment = Ripple.OperationPayment.newBuilder().apply {
                this.destination = params.input.destination()?.address ?: ""
                this.amount = params.finalAmount.toLong()
                this.destinationTag = try { params.input.memo()?.toLong() ?: 0L } catch (err: Throwable) { 0L }
            }.build()
        }.build()
        val output = AnySigner.sign(signInput, WCChainTypeProxy().invoke(chain), Ripple.SigningOutput.parser())
        return output.encoded.toByteArray()
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}