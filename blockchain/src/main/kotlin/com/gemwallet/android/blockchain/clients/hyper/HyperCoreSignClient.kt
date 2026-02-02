package com.gemwallet.android.blockchain.clients.hyper

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.services.mapper.toGem
import com.gemwallet.android.blockchain.services.toGem
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.wallet.core.primitives.Chain
import uniffi.gemstone.GemChainSigner
import uniffi.gemstone.GemGasPriceType
import uniffi.gemstone.GemTransactionInputType
import uniffi.gemstone.GemTransactionLoadInput
import uniffi.gemstone.PerpetualType
import java.math.BigInteger

class HyperCoreSignClient(
    val chain: Chain,
) : SignClient {

    private val hyperCore = GemChainSigner(chain.string)

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
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

    override suspend fun signPerpetualOpen(
        params: ConfirmParams.PerpetualParams.Open,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val metadata = (chainData as HyperCoreChainData).toGem()
        val gemLoadInput = GemTransactionLoadInput(
            inputType = GemTransactionInputType.Perpetual(
                asset = params.asset.toGem(),
                perpetualType = PerpetualType.Open(params.order.toGem())
            ),
            senderAddress = params.from.address,
            destinationAddress = params.from.address,
            value = finalAmount.toString(),
            gasPrice = GemGasPriceType.Regular("0"),
            memo = "",
            isMaxValue = params.useMaxAmount,
            metadata = metadata,
        )
        return super.signPerpetualOpen(params, chainData, finalAmount, fee, privateKey)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}