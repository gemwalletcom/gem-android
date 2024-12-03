package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Chain

class AptosSignerPreloader(
    private val chain: Chain,
    private val rpcClient: AptosRpcClient,
) : NativeTransferPreloader {

    private val feeCalculator = AptosFeeCalculator(chain, rpcClient)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        val sequence = try {
            rpcClient.accounts(params.from.address).getOrThrow().sequence_number?.toLong() ?: 0L
        } catch (_: Throwable) {
            0L
        }
        val fee = feeCalculator.calculate(params.destination().address)
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