package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.GasFee
import com.gemwallet.android.model.SignerParams
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

class BitcoinSignClient(
    private val chain: Chain,
) : SignClient {

    override suspend fun signTransfer(
        params: SignerParams,
        txSpeed: TxSpeed,
        privateKey: ByteArray
    ): ByteArray {
        val metadata = params.info as BitcoinSignerPreloader.Info
        val coinType = WCChainTypeProxy().invoke(maintainChain())
        val gasFee = metadata.fee(txSpeed) as GasFee
        val signingInput = Bitcoin.SigningInput.newBuilder().apply {
            this.coinType = coinType.value()
            this.hashType = BitcoinSigHashType.ALL.value()//BitcoinScript.hashTypeForCoin(coinType)
            this.amount = params.finalAmount.toLong()
            this.byteFee = gasFee.maxGasPrice.toLong()
            this.toAddress = params.input.destination()?.address
            this.changeAddress = params.owner
            this.useMaxAmount = params.input.isMax()
            this.addPrivateKey(ByteString.copyFrom(privateKey))
            this.addAllUtxo(metadata.utxo.getUtxoTransactions(params.owner, coinType))
            for (utxo in metadata.utxo) {
                val redeemScript = BitcoinScript.lockScriptForAddress(params.owner, coinType)
                val scriptData = redeemScript.data()
                if (coinType == CoinType.BITCOIN || scriptData?.isNotEmpty() == true) {
                    continue
                }
                val keyHash = if (redeemScript.isPayToWitnessPublicKeyHash) {
                    redeemScript.matchPayToWitnessPublicKeyHash()
                } else {
                    redeemScript.matchPayToPubkeyHash()
                }.toHexString()
                putScripts(keyHash, ByteString.copyFrom(redeemScript.data()))

            }
        }.build()
        val output = AnySigner.sign(signingInput, coinType, Bitcoin.SigningOutput.parser())
        if (output.error != Common.SigningError.OK) {
            throw IllegalStateException(output.error.name)
        }
        if (output.errorMessage.isNotEmpty()) {
            throw IllegalStateException(output.errorMessage)
        }
        return output.encoded.toByteArray()
    }

    override fun maintainChain(): Chain = chain
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