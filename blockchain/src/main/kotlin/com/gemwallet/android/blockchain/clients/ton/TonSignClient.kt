package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import uniffi.gemstone.GemFeeOption
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.proto.TheOpenNetwork
import java.math.BigInteger

class TonSignClient(
    private val chain: Chain,
) : SignClient {

    private val defaultMode: Int = TheOpenNetwork.SendMode.PAY_FEES_SEPARATELY_VALUE or TheOpenNetwork.SendMode.IGNORE_ACTION_PHASE_ERRORS_VALUE

    private val transferAllTonMode: Int = TheOpenNetwork.SendMode.ATTACH_ALL_CONTRACT_BALANCE_VALUE or defaultMode

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val chainData = (chainData as TonChainData)
        val message = TheOpenNetwork.Transfer.newBuilder().apply {
            this.dest = params.destination().address
            this.amount = ByteString.copyFrom(finalAmount.toByteArray())
            this.comment = params.memo() ?: ""
            this.mode = if (params.isMax()) {
                transferAllTonMode
            } else {
                defaultMode
            }
            this.bounceable = false
        }.build()
        return sign(
            sequence = chainData.sequence.toInt(),
            expireAt = chainData.expireAt,
            message = message,
            privateKey = privateKey
        )
    }

    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val meta = chainData as TonChainData

        val jettonTransfer = TheOpenNetwork.JettonTransfer.newBuilder().apply {
            this.jettonAmount = ByteString.copyFrom(finalAmount.toByteArray())
            this.toOwner = params.destination().address
            this.responseAddress = params.from.address
            this.forwardAmount = ByteString.copyFrom(BigInteger.ONE.toByteArray())
        }.build()

        val message = TheOpenNetwork.Transfer.newBuilder().apply {
            this.dest = meta.jettonAddress
            this.amount = ByteString.copyFrom((fee.options[GemFeeOption.TOKEN_ACCOUNT_CREATION.name] ?: BigInteger.ZERO).toByteArray())
            if (!params.memo().isNullOrEmpty()) {
                this.comment = params.memo()
            }
            this.jettonTransfer = jettonTransfer
            this.mode = defaultMode
            this.bounceable = true
        }.build()
        return sign(
            sequence = chainData.sequence.toInt(),
            expireAt = chainData.expireAt,
            message = message,
            privateKey = privateKey
        )
    }

    override suspend fun signSwap(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val data = params.swapData
        val chainData = (chainData as TonChainData)
        val message = TheOpenNetwork.Transfer.newBuilder().apply {
            this.dest = params.destination().address
            this.amount = ByteString.copyFrom(params.fromAmount.toByteArray())
            this.comment = params.memo() ?: ""
            this.mode = defaultMode
            this.bounceable = true
            this.customPayload = data
        }.build()
        return sign(
            sequence = chainData.sequence.toInt(),
            expireAt = chainData.expireAt,
            message = message,
            privateKey = privateKey
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private fun sign(
        sequence: Int,
        expireAt: Int?,
        message: TheOpenNetwork.Transfer,
        privateKey: ByteArray,
    ): List<ByteArray> {
        val signingInput = TheOpenNetwork.SigningInput.newBuilder().apply {
            this.sequenceNumber = sequence
            this.expireAt = expireAt ?: ((System.currentTimeMillis() / 1000).toInt() + 600)
            this.addMessages(message)
            this.walletVersion = TheOpenNetwork.WalletVersion.WALLET_V4_R2
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val output = AnySigner.sign(signingInput, CoinType.TON, TheOpenNetwork.SigningOutput.parser())
        return listOf(output.encoded.toByteArray())
    }
}