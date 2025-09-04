package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.SwapTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.blockchain.clients.aptos.services.AptosServices
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Chain

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
            response.sequence_number?.toULong() ?: 0UL
        } catch (_: Throwable) {
            0UL
        }
        val fee = feeCalculator.calculate(params, sequence)
        val input = SignerParams(params, AptosChainData(sequence), fee = fee)
        return input
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}