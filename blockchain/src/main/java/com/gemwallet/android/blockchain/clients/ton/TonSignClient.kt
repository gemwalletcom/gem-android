package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.proto.TheOpenNetwork
import java.math.BigInteger

class TonSignClient(
    private val chain: Chain,
) : SignClient {

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = (chainData as TonSignerPreloader.TonChainData)
        val signingInput = TheOpenNetwork.SigningInput.newBuilder().apply {
            sequenceNumber = chainData.sequence
            expireAt = chainData.expireAt ?: ((System.currentTimeMillis() / 1000).toInt() + 600)
            this.addMessages(
                TheOpenNetwork.Transfer.newBuilder().apply {
                    this.dest = params.destination().address
                    this.amount = finalAmount.toLong()
                    this.comment = params.memo() ?: ""
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

    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        val meta = chainData as TonSignerPreloader.TonChainData

        val jettonTransfer = TheOpenNetwork.JettonTransfer.newBuilder().apply {
            this.jettonAmount = finalAmount.toLong()
            this.toOwner = params.destination().address
            this.responseAddress = params.from.address
            this.forwardAmount = 1
        }.build()

        val transfer = TheOpenNetwork.Transfer.newBuilder().apply {
            this.dest = meta.jettonAddress
            this.amount = (meta.fee().options[tokenAccountCreationKey] ?: BigInteger.ZERO).toLong()
            if (!params.memo().isNullOrEmpty()) {
                this.comment = params.memo()
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
            this.expireAt = meta.expireAt ?: ((System.currentTimeMillis() / 1000).toInt() + 600)
        }.build()
        val output = AnySigner.sign(signingInput, CoinType.TON, TheOpenNetwork.SigningOutput.parser())
            .encoded.toByteArray()
        return listOf(output)
    }

    override suspend fun signSwap(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = params.swapData
        val chainData = (chainData as TonSignerPreloader.TonChainData)
        val signingInput = TheOpenNetwork.SigningInput.newBuilder().apply {
            sequenceNumber = chainData.sequence
            expireAt = chainData.expireAt ?: ((System.currentTimeMillis() / 1000).toInt() + 600)
            this.addMessages(
                TheOpenNetwork.Transfer.newBuilder().apply {
                    this.dest = params.destination().address
                    this.amount = params.fromAmount.toLong()
                    this.comment = params.memo() ?: ""
                    this.mode = TheOpenNetwork.SendMode.PAY_FEES_SEPARATELY_VALUE or TheOpenNetwork.SendMode.IGNORE_ACTION_PHASE_ERRORS_VALUE
                    this.bounceable = true
                    this.customPayload = data
                }.build()
            )
            this.walletVersion = TheOpenNetwork.WalletVersion.WALLET_V4_R2
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val output = AnySigner.sign(signingInput, CoinType.TON, TheOpenNetwork.SigningOutput.parser())
        return listOf(output.encoded.toByteArray())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}