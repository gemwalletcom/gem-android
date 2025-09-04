package com.gemwallet.android.blockchain.clients.algorand

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.algorand.services.AlgorandService
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority

class AlgorandSignPreloadClient(
    private val chain: Chain,
    private val feeService: AlgorandService,
) : NativeTransferPreloader {

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        val txParams = feeService.transactionsParams().getOrNull() ?: throw Exception("fee load error")
        val fee = Fee(
            priority = FeePriority.Normal,
            feeAssetId = AssetId(chain),
            amount = txParams.min_fee.toBigInteger(),
        )
        return SignerParams(
            input = params,
            chainData = AlgorandChainData(
                sequence = txParams.last_round.toULong(),
                block = txParams.genesis_hash,
                chainId = txParams.genesis_id,
            ),
            fee = listOf(fee)
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}