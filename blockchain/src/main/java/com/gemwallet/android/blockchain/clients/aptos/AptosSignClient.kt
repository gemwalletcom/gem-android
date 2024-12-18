package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.proto.Aptos
import wallet.core.jni.proto.Aptos.TokenTransferCoinsMessage
import wallet.core.jni.proto.Aptos.TransferMessage

class AptosSignClient(
    private val chain: Chain,
) : SignClient {
    override suspend fun signTransfer(params: SignerParams, txSpeed: TxSpeed, privateKey: ByteArray): ByteArray {
        val coinType = WCChainTypeProxy().invoke(chain)
        val metadata = params.chainData as AptosSignerPreloader.AptosChainData
        val fee = metadata.gasGee()
        val signInput = Aptos.SigningInput.newBuilder().apply {
            this.chainId = 1
            when (params.input) {
                is ConfirmParams.Stake -> throw IllegalArgumentException()
                is ConfirmParams.SwapParams -> TODO()
                is ConfirmParams.TokenApprovalParams -> TODO()
                is ConfirmParams.TransferParams.Native -> this.transfer = buildTransferMessage(params)
                is ConfirmParams.TransferParams.Token -> this.tokenTransferCoins = buildTokenCoinMessage(params)
            }
            this.expirationTimestampSecs = 3664390082
            this.gasUnitPrice = fee.maxGasPrice.toLong()
            this.maxGasAmount = fee.limit.toLong()
            this.sequenceNumber = metadata.sequence
            this.sender = params.input.from.address
            this.privateKey = ByteString.copyFrom(privateKey)
        }.build()
        val output = AnySigner.sign(signInput, coinType, Aptos.SigningOutput.parser())
        if (output.errorMessage.isNullOrEmpty()) {
            return output.json.toByteArray()
        } else {
            throw Exception(output.errorMessage)
        }
    }

    private fun buildTransferMessage(params: SignerParams): TransferMessage? {
        return TransferMessage.newBuilder().apply {
            this.to = params.input.destination()?.address
            this.amount = params.finalAmount.toLong()
        }.build()
    }

    private fun buildTokenCoinMessage(params: SignerParams): TokenTransferCoinsMessage? {
        val parts = params.input.assetId.tokenId?.split("::") ?: throw Exception("Bad asset id: wait token")
        val accountAddress = parts.firstOrNull() ?: throw Exception("Bad token: no account address")
        val module = parts.getOrNull(1) ?: throw Exception("Bad token: no module")
        val name = parts.getOrNull(2) ?: throw Exception("Bad token: no name")

        return TokenTransferCoinsMessage.newBuilder().apply {
            this.to = params.input.destination()!!.address
            this.amount = params.finalAmount.toLong()
            this.function = Aptos.StructTag.newBuilder().apply {
                this.accountAddress = accountAddress
                this.module = module
                this.name = name
            }.build()
        }.build()
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}