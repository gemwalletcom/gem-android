package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.StellarPassphrase
import wallet.core.jni.proto.Stellar
import java.math.BigInteger

class StellarSignClient(
    private val chain: Chain
) : SignClient {

    override suspend fun sign(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = (chainData as? StellarSignPreloadClient.StellarChainData)
            ?: throw Exception("bad params")
        val input = Stellar.SigningInput.newBuilder().apply {
            this.passphrase = StellarPassphrase.STELLAR.toString()
            this.fee = chainData.fee(txSpeed).amount.toInt()
            this.sequence = chainData.sequence
            this.account = params.from.address
            if (!params.memo().isNullOrEmpty()) {
                this.memoText = Stellar.MemoText.newBuilder().apply {
                    this.text = params.memo()!!
                }.build()
            }
            if (chainData.fee(txSpeed).options.contains(StellarSignPreloadClient.StellarChainData.tokenAccountCreation)) {
                this.opCreateAccount = Stellar.OperationCreateAccount.newBuilder().apply {
                    this.destination = params.destination().address
                    this.amount = finalAmount.toLong()
                }.build()
            } else {
                this.opPayment = Stellar.OperationPayment.newBuilder().apply {
                    this.destination = params.destination().address
                    this.amount = finalAmount.toLong()
                }.build()
            }
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val output = AnySigner.sign(input, WCChainTypeProxy().invoke(chain), Stellar.SigningOutput.parser())
        if (!output.errorMessage.isNullOrEmpty()) {
            throw Exception(output.errorMessage)
        }
        return listOf(output.signature.toByteArray())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}