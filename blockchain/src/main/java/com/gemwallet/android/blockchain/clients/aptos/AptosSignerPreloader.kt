package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.blockchain.clients.aptos.services.AptosAccountsService
import com.gemwallet.android.blockchain.clients.aptos.services.AptosFeeService
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority

class AptosSignerPreloader(
    private val chain: Chain,
    private val accountsService: AptosAccountsService,
    feeService: AptosFeeService,
) : NativeTransferPreloader, TokenTransferPreloader {

    private val feeCalculator = AptosFeeCalculator(chain, feeService)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        return preloadTransfer(params)
    }

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams {
        return preloadTransfer(params)
    }

    suspend fun preloadTransfer(params: ConfirmParams.TransferParams): SignerParams {
        val sequence = try {
            val response = accountsService.accounts(params.from.address).getOrThrow()
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