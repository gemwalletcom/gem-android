package com.gemwallet.android.blockchain.clients.algorand

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.algorand.services.AlgorandNodeStatusService
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain

class AlgorandSignPreloadClient(
    private val chain: Chain,
    private val feeService: AlgorandNodeStatusService,
) : NativeTransferPreloader {

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        val txParams = feeService.transactionsParams().getOrNull() ?: throw Exception("fee load error")
        val fee = Fee(
            speed = TxSpeed.Normal,
            feeAssetId = AssetId(chain),
            amount = txParams.min_fee.toBigInteger(),
        )
        return SignerParams(
            input = params,
            chainData = AlgorandChainData(
                sequence = txParams.last_round.toInt(),
                block = txParams.genesis_hash,
                chainId = txParams.genesis_id,
                fee = fee
            )
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    data class AlgorandChainData(
        val sequence: Int,
        val block: String,
        val chainId: String,
        val fee: Fee,
    ) : ChainSignData {
        override fun fee(speed: TxSpeed): Fee = fee
    }
}