package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fee
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.SwapProvider
import wallet.core.java.AnySigner
import wallet.core.jni.proto.Ripple
import wallet.core.jni.proto.Ripple.OperationPayment
import wallet.core.jni.proto.Ripple.OperationTrustSet
import java.math.BigInteger

class XrpSignClient(
    private val chain: Chain,
) : SignClient {
    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val operation = OperationPayment.newBuilder().apply {
            this.destination = params.destination().address
            this.amount = finalAmount.toLong()
            this.destinationTag = try { params.memo()?.toLong() ?: 0 } catch (_: Throwable) { 0 }
        }.build()
        return sign(params, chainData, fee, operation, privateKey)
    }

    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val operation = OperationPayment.newBuilder().apply {
            this.destination = params.destination().address
            this.currencyAmount = Ripple.CurrencyAmount.newBuilder().apply {
                this.issuer = params.assetId.tokenId!!
                this.currency = hexSymbol(params.asset.symbol)
                this.value = Crypto(finalAmount).value(params.asset.decimals).toString()
            }.build()
            this.destinationTag = try { params.memo()?.toLong() ?: 0 } catch (_: Throwable) { 0L }
        }.build()
        return sign(params, chainData, fee, operation, privateKey)
    }

    override suspend fun signActivate(
        params: ConfirmParams.Activate,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val operation = OperationTrustSet.newBuilder().apply {
            this.limitAmount = Ripple.CurrencyAmount.newBuilder().apply {
                this.value = "690000000000"
                this.currency = hexSymbol(params.asset.symbol)
                this.issuer = params.assetId.tokenId!!
            }.build()
        }.build()
        return sign(params, chainData, fee, operation, privateKey)
    }

    override suspend fun signSwap(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        when (params.protocolId) {
            SwapProvider.Thorchain.string -> {
                val json = """
                {
                    "TransactionType": "Payment",
                    "Destination": "${params.to}",
                    "Amount": "${params.value}",
                    "Memos": [
                        {
                            "Memo": {
                                "MemoData": "${params.swapData/*.removePrefix("0x")*/.toByteArray().toHexString("")}"
                            }
                        }
                    ]
                }
                """
                return sign(params, chainData, fee, json, privateKey)
            }
            else -> throw Exception("Provider doesn't support")
        }
    }

    private fun sign(
        params: ConfirmParams,
        chainData: ChainSignData,
        fee: Fee,
        operation: Any,
        privateKey: ByteArray,
    ): List<ByteArray> {
        val metadata = chainData as XrpChainData
        val signInput = Ripple.SigningInput.newBuilder().apply {
            this.fee = fee.amount.toLong()
            this.sequence = metadata.sequence
            this.account = params.from.address
            this.privateKey = ByteString.copyFrom(privateKey)
            this.lastLedgerSequence = (metadata.blockNumber + 12)
            when (operation) {
                is String -> this.rawJson = operation
                is OperationPayment -> this.opPayment = operation
                is OperationTrustSet -> this.opTrustSet = operation
            }
        }.build()
        val output = AnySigner.sign(signInput, WCChainTypeProxy().invoke(chain), Ripple.SigningOutput.parser())
        return listOf(output.encoded.toByteArray().toHexString("").toByteArray())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private fun hexSymbol(symbol: String): String {
        return symbol.toByteArray().toHexString("").uppercase().padEnd(40, '0')
    }
}