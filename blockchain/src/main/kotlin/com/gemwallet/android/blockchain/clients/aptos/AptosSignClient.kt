package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
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
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        return sign(params, chainData, fee as GasFee, privateKey, buildTransferMessage(params, finalAmount))
    }

    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        return sign(params, chainData, fee as GasFee, privateKey, buildTokenCoinMessage(params, finalAmount))
    }

    override suspend fun signSwap(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        return sign(params, chainData, fee as GasFee, privateKey, params.swapData)
    }

    private fun sign(params: ConfirmParams, chainData: ChainSignData, fee: GasFee, privateKey: ByteArray, message: Any): List<ByteArray> {
        val metadata = chainData as AptosChainData
        val signInput = Aptos.SigningInput.newBuilder().apply {
            this.chainId = 1
            when (message) {
                is TransferMessage -> this.transfer = message
                is TokenTransferCoinsMessage -> this.tokenTransferCoins = message
                is String -> this.anyEncoded = message
                else -> IllegalArgumentException()
            }
            this.expirationTimestampSecs = 3664390082
            this.gasUnitPrice = fee.maxGasPrice.toLong()
            this.maxGasAmount = fee.limit.toLong()
            this.sequenceNumber = metadata.sequence.toLong()
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
        val parts = params.asset.id.tokenId?.split("::") ?: throw Exception("Bad asset id: wait token")
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