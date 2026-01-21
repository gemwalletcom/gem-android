package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.SignClient
import com.gemwallet.android.blockchain.services.mapper.toGem
import com.gemwallet.android.domains.confirm.toGem
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.GasFee
import com.wallet.core.primitives.Chain
import uniffi.gemstone.GemChainSigner
import uniffi.gemstone.GemGasPriceType
import uniffi.gemstone.GemTransactionInputType
import uniffi.gemstone.GemTransactionLoadInput
import java.math.BigInteger

class AptosSignClient(
    private val chain: Chain,
) : SignClient {

    val gemSigner = GemChainSigner(chain.string)

    override suspend fun signNativeTransfer(
        params: ConfirmParams.TransferParams.Native,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val metadata = (chainData as AptosChainData).toGem()
        val gemLoadInput = GemTransactionLoadInput(
            inputType = GemTransactionInputType.Transfer(params.asset.toGem()),
            senderAddress = params.from.address,
            destinationAddress = params.destination.address,
            value = finalAmount.toString(),
            gasPrice = GemGasPriceType.Regular((fee as? GasFee)?.maxGasPrice.toString()),
            memo = params.memo,
            isMaxValue = params.useMaxAmount,
            metadata = metadata,
        )
        return listOf(gemSigner.signTransfer(gemLoadInput, privateKey).toByteArray())
    }

    override suspend fun signTokenTransfer(
        params: ConfirmParams.TransferParams.Token,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val metadata = (chainData as AptosChainData).toGem()
        val gemLoadInput = GemTransactionLoadInput(
            inputType = GemTransactionInputType.Transfer(params.asset.toGem()),
            senderAddress = params.from.address,
            destinationAddress = params.destination.address,
            value = finalAmount.toString(),
            gasPrice = GemGasPriceType.Regular((fee as? GasFee)?.maxGasPrice.toString()),
            memo = params.memo,
            isMaxValue = params.useMaxAmount,
            metadata = metadata,
        )
        return listOf(gemSigner.signTransfer(gemLoadInput, privateKey).toByteArray())
    }

    override suspend fun signSwap(
        params: ConfirmParams.SwapParams,
        chainData: ChainSignData,
        finalAmount: BigInteger,
        fee: Fee,
        privateKey: ByteArray
    ): List<ByteArray> {
        val metadata = (chainData as AptosChainData).toGem()
        val gemLoadInput = GemTransactionLoadInput(
            inputType = GemTransactionInputType.Swap(params.fromAsset.toGem(), params.toAsset.toGem(), params.toGem()),
            senderAddress = params.from.address,
            destinationAddress = params.toAddress,
            value = finalAmount.toString(),
            gasPrice = GemGasPriceType.Regular((fee as? GasFee)?.maxGasPrice.toString()),
            memo = params.memo,
            isMaxValue = params.useMaxAmount,
            metadata = metadata,
        )
        return listOf(gemSigner.signTransfer(gemLoadInput, privateKey).toByteArray())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}