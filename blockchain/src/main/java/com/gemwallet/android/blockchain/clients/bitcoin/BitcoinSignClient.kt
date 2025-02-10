package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.TxSpeed
import com.google.protobuf.ByteString
import com.wallet.core.blockchain.bitcoin.models.BitcoinUTXO
import com.wallet.core.primitives.Chain
import wallet.core.java.AnySigner
import wallet.core.jni.BitcoinScript
import wallet.core.jni.BitcoinSigHashType
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Bitcoin
import wallet.core.jni.proto.Common
import java.math.BigInteger

class BitcoinSignClient(
    private val chain: Chain,
) : SignClient {

    val coinType = WCChainTypeProxy().invoke(chain)

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        val signingInput = getSigningInput(
            params,
            chainData,
            finalAmount,
            txSpeed,
            privateKey
        )
        return sign(signingInput.build())
    }

    override suspend fun signSwap(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): List<ByteArray> {
        val signingInput = getSigningInput(
            params,
            chainData,
            finalAmount,
            txSpeed,
            privateKey
        )
        signingInput.outputOpReturn = ByteString.copyFrom(params.swapData.toByteArray())
        return sign(signingInput.build())
    }

    private fun getSigningInput(
        params: ConfirmParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): Bitcoin.SigningInput.Builder {
        val chainData = chainData as BitcoinSignerPreloader.BitcoinChainData
        val gasFee = chainData.fee(txSpeed) as GasFee
        val coinType = coinType

        return Bitcoin.SigningInput.newBuilder().apply {
            this.coinType = coinType.value()
            this.hashType = BitcoinSigHashType.ALL.value()
            this.amount = finalAmount.toLong()
            this.byteFee = gasFee.maxGasPrice.toLong()
            this.toAddress = params.destination()?.address
            this.changeAddress = params.from.address
            this.useMaxAmount = params.isMax()

            this.addPrivateKey(ByteString.copyFrom(privateKey))
            this.addAllUtxo(chainData.utxo.getUtxoTransactions(params.from.address, coinType))
            chainData.utxo.forEach {
                val redeemScript = BitcoinScript.lockScriptForAddress(params.from.address, coinType)
                val scriptData = redeemScript.data()
                if (coinType == CoinType.BITCOIN || scriptData?.isNotEmpty() == true) {
                    return@forEach
                }
                val keyHash = if (redeemScript.isPayToWitnessPublicKeyHash) {
                    redeemScript.matchPayToWitnessPublicKeyHash()
                } else {
                    redeemScript.matchPayToPubkeyHash()
                }.toHexString()
                putScripts(keyHash, ByteString.copyFrom(redeemScript.data()))
            }
        }
    }

    private fun sign(signingInput: Bitcoin.SigningInput): List<ByteArray> {
        val output = AnySigner.sign(signingInput, coinType, Bitcoin.SigningOutput.parser())
        if (output.error != Common.SigningError.OK) {
            throw IllegalStateException(output.error.name)
        }
        if (output.errorMessage.isNotEmpty()) {
            throw IllegalStateException(output.errorMessage)
        }
        return listOf(output.encoded.toByteArray())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}

fun List<BitcoinUTXO>.getUtxoTransactions(address: String, coinType: CoinType): List<Bitcoin.UnspentTransaction> {
    return map { utxo ->
        Bitcoin.UnspentTransaction.newBuilder().apply {
            val hash = utxo.txid.decodeHex()
            hash.reverse()
            this.outPoint = Bitcoin.OutPoint.newBuilder().apply {
                this.hash = ByteString.copyFrom(hash)
                this.index = utxo.vout
            }.build()
            this.amount = utxo.value.toLong()
            this.script = ByteString.copyFrom(
                BitcoinScript.lockScriptForAddress(address, coinType).data()
            )
        }.build()
    }
}