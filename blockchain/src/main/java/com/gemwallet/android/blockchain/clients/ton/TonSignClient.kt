package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.ext.type
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.proto.TheOpenNetwork
import java.math.BigInteger

class TonSignClient(
    private val chain: Chain,
) : SignClient {
    override suspend fun signTransaction(
        params: SignerParams,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        if (params.input.assetId.type() == AssetSubtype.TOKEN) {
            return signToken(params, privateKey)
        }
        val signingInput = TheOpenNetwork.SigningInput.newBuilder().apply {
            sequenceNumber = (params.chainData as TonSignerPreloader.TonChainData).sequence
            expireAt = (System.currentTimeMillis() / 1000).toInt() + 600
            this.addMessages(
                TheOpenNetwork.Transfer.newBuilder().apply {
                    this.dest = params.input.destination()?.address
                    this.amount = params.finalAmount.toLong()
                    this.comment = params.input.memo() ?: ""
                    this.mode = TheOpenNetwork.SendMode.PAY_FEES_SEPARATELY_VALUE or TheOpenNetwork.SendMode.IGNORE_ACTION_PHASE_ERRORS_VALUE
                    this.bounceable = false
                }.build()
            )
            this.walletVersion = TheOpenNetwork.WalletVersion.WALLET_V4_R2
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val output = AnySigner.sign(signingInput, CoinType.TON, TheOpenNetwork.SigningOutput.parser())
        return listOf(output.encoded.toByteArray())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private fun signToken(params: SignerParams, privateKey: ByteArray): List<ByteArray> {
        val meta = params.chainData as TonSignerPreloader.TonChainData

        val jettonTransfer = TheOpenNetwork.JettonTransfer.newBuilder().apply {
            this.jettonAmount = params.finalAmount.toLong()
            this.toOwner = params.input.destination()?.address
            this.responseAddress = params.input.from.address
            this.forwardAmount = 1
        }.build()

        val transfer = TheOpenNetwork.Transfer.newBuilder().apply {
            this.dest = meta.jettonAddress
            this.amount = (meta.fee().options[tokenAccountCreationKey] ?: BigInteger.ZERO).toLong()
            if (!params.input.memo().isNullOrEmpty()) {
                this.comment = params.input.memo()
            }
            this.jettonTransfer = jettonTransfer
            this.mode = TheOpenNetwork.SendMode.PAY_FEES_SEPARATELY_VALUE or TheOpenNetwork.SendMode.IGNORE_ACTION_PHASE_ERRORS_VALUE
            this.bounceable = true
        }.build()

        val signingInput = TheOpenNetwork.SigningInput.newBuilder().apply {
            this.walletVersion = TheOpenNetwork.WalletVersion.WALLET_V4_R2
            this.sequenceNumber = meta.sequence
            this.addMessages(transfer)
            this.privateKey = ByteString.copyFrom(privateKey)
            this.expireAt = (System.currentTimeMillis() / 1000).toInt() + 600
        }.build()
        val output = AnySigner.sign(signingInput, CoinType.TON, TheOpenNetwork.SigningOutput.parser())
            .encoded.toByteArray()
        return listOf(output)
    }
}