package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import uniffi.gemstone.FeeOption
import wallet.core.java.AnySigner
import wallet.core.jni.StellarPassphrase
import wallet.core.jni.proto.Stellar
import java.math.BigInteger

class StellarSignClient(
    private val chain: Chain
) : SignClient {

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = (chainData as? StellarChainData)
            ?: throw Exception("bad params")
        val input = Stellar.SigningInput.newBuilder().apply {
            this.passphrase = StellarPassphrase.STELLAR.toString()
            this.fee = fee.amount.toInt()
            this.sequence = chainData.sequence.toLong()
            this.account = params.from.address
            if (!params.memo().isNullOrEmpty()) {
                this.memoText = Stellar.MemoText.newBuilder().apply {
                    this.text = params.memo()!!
                }.build()
            }
            if (fee.options.contains(FeeOption.TOKEN_ACCOUNT_CREATION.name)) {
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