package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.math.decodeHex
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.google.protobuf.ByteString
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.SwapProvider
import com.wallet.core.primitives.UTXO
import wallet.core.java.AnySigner
import wallet.core.jni.BitcoinScript
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Bitcoin
import wallet.core.jni.proto.Common
import java.math.BigInteger
import kotlin.math.max

class BitcoinSignClient(
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
        val signingInput = getSigningInput(
            params,
            chainData,
            finalAmount,
            fee,
            privateKey
        )
        return sign(signingInput.build())
    }

    override suspend fun signSwap(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val providers = setOf(SwapProvider.Thorchain, SwapProvider.Chainflip)
            .map { it.string }
        if (!providers.contains(params.protocolId)) {
            throw Exception("Invalid signing input type or not supported provider id")
        }
        if (params.isMax() && params.protocolId == SwapProvider.Chainflip.string) {
            throw Exception("Invalid signing input type or not supported provider id")
        }
        val signingInput = getSigningInput(
            params,
            chainData,
            finalAmount,
            fee,
            privateKey,
        )
        when(params.protocolId) {
            SwapProvider.Chainflip.string -> {
                signingInput.outputOpReturnIndex = Bitcoin.OutputIndex.newBuilder().apply { index = 1 }.build()
                signingInput.outputOpReturn = try {
                    ByteString.copyFrom(params.swapData.decodeHex())
                } catch (_: Throwable) {
                    throw Exception("Invalid Chainflip swap data")
                }
            }
            else -> {
                signingInput.outputOpReturn = ByteString.copyFrom(params.swapData.toByteArray())
            }
        }

        return sign(signingInput.build())
    }

    private fun getSigningInput(
        params: ConfirmParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): Bitcoin.SigningInput.Builder {
        return when (chain) {
            Chain.Zcash -> getSigningInputZcash(params, chainData, finalAmount, fee, privateKey)
            else -> getSigningInputBitcoin(params, chainData, finalAmount, fee, privateKey)
        }
    }

    private fun getSigningInputBitcoin(
        params: ConfirmParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): Bitcoin.SigningInput.Builder {
        val chainData = chainData as BitcoinChainData
        val gasFee = fee as GasFee
        val coinType = coinType

        return Bitcoin.SigningInput.newBuilder().apply {
            this.coinType = coinType.value()
            this.hashType = BitcoinScript.hashTypeForCoin(coinType)
            this.amount = finalAmount.toLong()
            this.byteFee = gasFee.maxGasPrice.toLong()
            this.toAddress = params.destination()?.address
            this.changeAddress = params.from.address
            this.useMaxAmount = params.isMax()
            this.addPrivateKey(ByteString.copyFrom(privateKey))
            this.addAllUtxo(chainData.utxo.getUtxoTransactions(params.from.address, coinType))
            chainData.utxo.forEach { _ ->
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

    private fun getSigningInputZcash(
        params: ConfirmParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): Bitcoin.SigningInput.Builder {
        val chainData = chainData as ZCashChainData
        val coinType = coinType
        val signingInput = Bitcoin.SigningInput.newBuilder().apply {
            this.coinType = coinType.value()
            this.hashType = BitcoinScript.hashTypeForCoin(coinType)
//            this.amount = finalAmount.toLong()
            this.byteFee = 0
            this.toAddress = params.destination()?.address
            this.changeAddress = params.from.address
            this.useMaxAmount = params.isMax()
            this.addAllUtxo(chainData.utxo.getUtxoTransactions(params.from.address, coinType))
            chainData.utxo.forEach { _ ->
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
            this.addPrivateKey(ByteString.copyFrom(privateKey))
        }
        val totalAvailable = chainData.utxo.fold(BigInteger.ZERO) { acc, uTXO -> acc + (uTXO.value.toBigIntegerOrNull() ?: BigInteger.ZERO) }.toLong()
        val fee = fee.amount.toLong()
        val requestAmount = finalAmount.toLong()
        val targetAmount = if (params.isMax()) max(totalAvailable - fee, 0) else requestAmount
        if ((totalAvailable - fee) < targetAmount) {
            throw IllegalStateException()
        }
        val change = max(totalAvailable - targetAmount - fee, 0)
        signingInput.amount = targetAmount
        signingInput.plan = Bitcoin.TransactionPlan.newBuilder().apply {
            this.amount = targetAmount
            this.availableAmount = totalAvailable
            this.fee = fee
            this.change = change
            this.addAllUtxos(chainData.utxo.getUtxoTransactions(params.from.address, coinType))
            this.branchId = ByteString.copyFrom(chainData.branchId.toByteArray())
        }.build()

        return signingInput
    }

    private fun sign(signingInput: Bitcoin.SigningInput): List<ByteArray> {
        val output = AnySigner.sign(signingInput, coinType, Bitcoin.SigningOutput.parser())
        if (output.error != Common.SigningError.OK) {
            throw IllegalStateException(output.error.name)
        }
        if (output.errorMessage.isNotEmpty()) {
            throw IllegalStateException(output.errorMessage)
        }
        return listOf(output.encoded.toByteArray().toHexString("").toByteArray())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}

fun List<UTXO>.getUtxoTransactions(address: String, coinType: CoinType): List<Bitcoin.UnspentTransaction> {
    return map { utxo ->
        Bitcoin.UnspentTransaction.newBuilder().apply {
            val hash = utxo.transaction_id.decodeHex()
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