package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.blockchain.clients.aptos.services.AptosServices
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority

class AptosSignerPreloader(
    private val chain: Chain,
    private val aptosServices: AptosServices,
) : NativeTransferPreloader, TokenTransferPreloader, SwapTransactionPreloader {

    private val feeCalculator = AptosFeeCalculator(chain, aptosServices)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        return preloadTransfer(params)
    }

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams {
        return preloadTransfer(params)
    }

    override suspend fun preloadSwap(params: ConfirmParams.SwapParams): SignerParams {
        return preloadTransfer(params)
    }

    private suspend fun preloadTransfer(params: ConfirmParams): SignerParams {
        val sequence = try {
            val response = aptosServices.accounts(params.from.address).getOrThrow()
            response.sequence_number?.toLong() ?: 0L
        } catch (_: Throwable) {
            0L
        }
        val fee = feeCalculator.calculate(params, sequence)
        val input = SignerParams(params, AptosChainData(sequence, fee))
        return input
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    data class AptosChainData(
        val sequence: Long,
        val fees: List<Fee>,
    ) : ChainSignData {
        override fun fee(speed: FeePriority): Fee = fees.firstOrNull { it.priority == speed } ?: fees.first()

        override fun allFee(): List<Fee> = fees
    }
}