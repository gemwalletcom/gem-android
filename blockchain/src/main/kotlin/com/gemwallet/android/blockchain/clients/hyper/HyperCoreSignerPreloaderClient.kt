package com.gemwallet.android.blockchain.clients.hyper

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.StakeTransactionPreloader
import com.gemwallet.android.blockchain.clients.TokenTransferPreloader
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.SignerParams
import com.wallet.core.primitives.Chain

class HyperCoreSignerPreloaderClient(
    private val chain: Chain,
) : NativeTransferPreloader, TokenTransferPreloader, StakeTransactionPreloader {
    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        TODO("Not yet implemented")
    }

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams {
        TODO("Not yet implemented")
    }

    override suspend fun preloadStake(params: ConfirmParams.Stake): SignerParams {
        TODO("Not yet implemented")
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}
