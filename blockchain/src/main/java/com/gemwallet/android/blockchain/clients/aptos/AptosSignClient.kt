package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import wallet.core.java.AnySigner
import wallet.core.jni.proto.Aptos
import wallet.core.jni.proto.Aptos.TokenTransferCoinsMessage
import wallet.core.jni.proto.Aptos.TransferMessage
import java.math.BigInteger

class AptosSignClient(
    private val chain: Chain,
) : SignClient {

    val coinType = WCChainTypeProxy().invoke(chain)

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        return sign(params, chainData, privateKey, buildTransferMessage(params, finalAmount))
    }

    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        feePriority: FeePriority,
        privateKey: ByteArray
    ): List<ByteArray> {
        return sign(params, chainData, privateKey, buildTokenCoinMessage(params, finalAmount))
    }

    private fun sign(params: ConfirmParams, chainData: ChainSignData, privateKey: ByteArray, message: Any): List<ByteArray> {
        val metadata = chainData as AptosSignerPreloader.AptosChainData
        val fee = metadata.gasFee()
        val signInput = Aptos.SigningInput.newBuilder().apply {
            this.chainId = 1
            when (message) {
                is TransferMessage -> this.transfer = message
                is TokenTransferCoinsMessage -> this.tokenTransferCoins = message
                else -> IllegalArgumentException()
            }
            this.expirationTimestampSecs = 3664390082
            this.gasUnitPrice = fee.maxGasPrice.toLong()
            this.maxGasAmount = fee.limit.toLong()
            this.sequenceNumber = metadata.sequence
            this.sender = params.from.address
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()

        val output = AnySigner.sign(signInput, coinType, Aptos.SigningOutput.parser())

        return if (output.errorMessage.isNullOrEmpty()) {
            listOf(output.json.toByteArray())
        } else {
            throw Exception(output.errorMessage)
        }
    }

    private fun buildTransferMessage(params: ConfirmParams.TransferParams.Native, finalAmount: BigInteger): TransferMessage {
        return TransferMessage.newBuilder().apply {
            this.to = params.destination().address
            this.amount = finalAmount.toLong()
        }.build()
    }

    private fun buildTokenCoinMessage(params: ConfirmParams.TransferParams.Token, finalAmount: BigInteger): TokenTransferCoinsMessage {
        val parts = params.assetId.tokenId?.split("::") ?: throw Exception("Bad asset id: wait token")
        val accountAddress = parts.firstOrNull() ?: throw Exception("Bad token: no account address")
        val module = parts.getOrNull(1) ?: throw Exception("Bad token: no module")
        val name = parts.getOrNull(2) ?: throw Exception("Bad token: no name")

        return TokenTransferCoinsMessage.newBuilder().apply {
            this.to = params.destination().address
            this.amount = finalAmount.toLong()
            this.function = Aptos.StructTag.newBuilder().apply {
                this.accountAddress = accountAddress
                this.module = module
                this.name = name
            }.build()
        }.build()
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}