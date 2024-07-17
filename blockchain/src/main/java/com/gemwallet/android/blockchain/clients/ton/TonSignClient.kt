package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.SignerParams
import com.google.protobuf.ByteString
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.proto.TheOpenNetwork
import java.math.BigInteger

class TonSignClient : SignClient {
    override suspend fun signTransfer(
        params: SignerParams,
        privateKey: ByteArray
    ): ByteArray {
        if (params.input.assetId.type() == AssetSubtype.TOKEN) {
            return signToken(params, privateKey)
        }
        val signingInput = TheOpenNetwork.SigningInput.newBuilder().apply {
            this.transfer = TheOpenNetwork.Transfer.newBuilder().apply {
                this.walletVersion = TheOpenNetwork.WalletVersion.WALLET_V4_R2
                this.dest = params.input.destination()?.address
                this.amount = params.finalAmount.toLong()
                this.comment = params.input.memo() ?: ""
                this.sequenceNumber = (params.info as TonSignerPreloader.Info).sequence
                this.mode = TheOpenNetwork.SendMode.PAY_FEES_SEPARATELY_VALUE or TheOpenNetwork.SendMode.IGNORE_ACTION_PHASE_ERRORS_VALUE
                this.bounceable = false
            }.build()
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val output = AnySigner.sign(signingInput, CoinType.TON, TheOpenNetwork.SigningOutput.parser())
        return output.encoded.toByteArray()
    }

    override fun maintainChain(): Chain = Chain.Ton

    private fun signToken(params: SignerParams, privateKey: ByteArray): ByteArray {
        val meta = params.info as TonSignerPreloader.Info
        val transfer = TheOpenNetwork.Transfer.newBuilder().apply {
            this.walletVersion = TheOpenNetwork.WalletVersion.WALLET_V4_R2
            this.dest = meta.jettonAddress
            this.amount = (meta.fee.options[tokenAccountCreationKey] ?: BigInteger.ZERO).toLong()
            if (!params.input.memo().isNullOrEmpty()) {
                this.comment = params.input.memo()
            }
            this.sequenceNumber = meta.sequence
            this.mode = TheOpenNetwork.SendMode.PAY_FEES_SEPARATELY_VALUE or TheOpenNetwork.SendMode.IGNORE_ACTION_PHASE_ERRORS_VALUE
            this.bounceable = true
        }.build()

        val jettonTransfer = TheOpenNetwork.JettonTransfer.newBuilder().apply {
            this.transfer = transfer
            this.jettonAmount = params.finalAmount.toLong()
            this.toOwner = params.input.destination()?.address
            this.responseAddress = params.owner
            this.forwardAmount = 1
        }.build()

        val signingInput = TheOpenNetwork.SigningInput.newBuilder().apply {
            this.jettonTransfer = jettonTransfer
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        return AnySigner.sign(signingInput, CoinType.TON, TheOpenNetwork.SigningOutput.parser())
            .encoded.toByteArray()
    }
}