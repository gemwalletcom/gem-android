package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.blockchain.clients.aptos.services.AptosAccountsService
import com.gemwallet.android.blockchain.clients.aptos.services.AptosFeeService
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Chain

class AptosSignerPreloader(
    private val chain: Chain,
    private val accountsService: AptosAccountsService,
    feeService: AptosFeeService,
) : NativeTransferPreloader, TokenTransferPreloader {

    private val feeCalculator = AptosFeeCalculator(chain, feeService, accountsService)

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
        val fee = feeCalculator.calculate(params.assetId, params.destination().address)
        val input = SignerParams(params, AptosChainData(sequence, fee))
        return input
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    data class AptosChainData(
        val sequence: Long,
        val fee: Fee,
    ) : ChainSignData {
        override fun fee(speed: TxSpeed): Fee = fee
    }
}