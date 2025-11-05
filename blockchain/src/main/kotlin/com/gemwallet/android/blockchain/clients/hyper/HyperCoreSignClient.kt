package com.gemwallet.android.blockchain.clients.hyper

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.services.mapper.toGem
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.wallet.core.primitives.Chain
import uniffi.gemstone.GemChainSigner
import uniffi.gemstone.GemGasPriceType
import uniffi.gemstone.GemTransactionInputType
import uniffi.gemstone.GemTransactionLoadInput
import java.math.BigInteger

class HyperCoreSignClient(
    val chain: Chain,
) : SignClient {

    private val hyperCore = GemChainSigner(chain.string)
//    private val factory = HyperCoreModelFactory()
//    private val agentNamePrefix = "gemwallet_"
//    private val referralCode = "GEMWALLET"
//    private val builderAddress = "0x0d9dab1a248f63b0a48965ba8435e4de7497a3dc"
    private val nativeSpotToken = "HYPE:0x0d01dc56dcaaca66ad901c959b4011ec"

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
//        val amount = params.asset.format(Crypto(finalAmount), decimalPlace = params.asset.decimals, showSymbol = false)
        val metadata = (chainData as HyperCoreChainData).toGem()
        val gemLoadInput = GemTransactionLoadInput(
            inputType = GemTransactionInputType.Transfer(params.asset.toGem()),
            senderAddress = params.from.address,
            destinationAddress = params.destination.address,
            value = finalAmount.toString(),
            gasPrice = GemGasPriceType.Regular("0"),
            memo = params.memo,
            isMaxValue = params.useMaxAmount,
            metadata = metadata,
        )
        return listOf(hyperCore.signTransfer(gemLoadInput, privateKey).toByteArray())
    }

    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val metadata = (chainData as HyperCoreChainData).toGem()
        val gemLoadInput = GemTransactionLoadInput(
            inputType = GemTransactionInputType.Transfer(params.asset.toGem()),
            senderAddress = params.from.address,
            destinationAddress = params.destination.address,
            value = finalAmount.toString(),
            gasPrice = GemGasPriceType.Regular("0"),
            memo = params.memo,
            isMaxValue = params.useMaxAmount,
            metadata = metadata,
        )
        return listOf(hyperCore.signTokenTransfer(gemLoadInput, privateKey).toByteArray())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}