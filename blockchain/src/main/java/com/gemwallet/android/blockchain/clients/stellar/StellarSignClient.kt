package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.StellarPassphrase
import wallet.core.jni.proto.Stellar

class StellarSignClient(
    private val chain: Chain
) : SignClient {

    override suspend fun signTransfer(
        params: SignerParams,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): ByteArray {
        val chainData = (params.chainData as? StellarSignPreloadClient.StellarChainData)
            ?: throw Exception("bad params")
        val input = Stellar.SigningInput.newBuilder().apply {
            this.passphrase = StellarPassphrase.STELLAR.toString()
            this.fee = chainData.fee(txSpeed).amount.toInt()
            this.sequence = chainData.sequence
            this.account = params.input.from.address
            if (!params.input.memo().isNullOrEmpty()) {
                this.memoText = Stellar.MemoText.newBuilder().apply {
                    this.text = params.input.memo()!!
                }.build()
            }
            if (chainData.fee(txSpeed).options.contains(StellarSignPreloadClient.StellarChainData.tokenAccountCreation)) {
                this.opCreateAccount = Stellar.OperationCreateAccount.newBuilder().apply {
                    this.destination = params.input.destination()!!.address
                    this.amount = params.finalAmount.toLong()
                }.build()
            } else {
                this.opPayment = Stellar.OperationPayment.newBuilder().apply {
                    this.destination = params.input.destination()!!.address
                    this.amount = params.finalAmount.toLong()
                }.build()
            }
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val output = AnySigner.sign(input, WCChainTypeProxy().invoke(chain), Stellar.SigningOutput.parser())
        if (!output.errorMessage.isNullOrEmpty()) {
            throw Exception(output.errorMessage)
        }
        return output.signature.toByteArray()
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}