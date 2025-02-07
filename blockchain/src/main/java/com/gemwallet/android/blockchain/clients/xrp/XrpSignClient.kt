package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.proto.Ripple
import java.math.BigInteger

class XrpSignClient(
    private val chain: Chain,
) : SignClient {
    override suspend fun sign(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        val metadata = chainData as XrpSignerPreloader.XrpChainData
        val signInput = Ripple.SigningInput.newBuilder().apply {
            this.fee = metadata.fee().amount.toLong()
            this.sequence = metadata.sequence
            this.account = params.from.address
            this.privateKey = ByteString.copyFrom(privateKey)
            this.opPayment = Ripple.OperationPayment.newBuilder().apply {
                this.destination = params.destination().address
                this.amount = finalAmount.toLong()
                this.destinationTag = try { params.memo()?.toLong() ?: 0L } catch (_: Throwable) { 0L }
            }.build()
        }.build()
        val output = AnySigner.sign(signInput, WCChainTypeProxy().invoke(chain), Ripple.SigningOutput.parser())
        return listOf(output.encoded.toByteArray())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}