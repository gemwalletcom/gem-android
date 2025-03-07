package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.NativeTransferPreloader
import com.gemwallet.android.blockchain.clients.xrp.services.account
import com.gemwallet.android.model.ChainSignData
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class XrpSignerPreloader(
    private val chain: Chain,
    private val rpcClient: XrpRpcClient,
) : NativeTransferPreloader {

    private val feeCalculator = XrpFeeCalculator(chain, rpcClient)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams = withContext(Dispatchers.IO) {
        val (getSequence, getFee) = Pair (
            async {
                rpcClient.account(params.from.address).getOrNull()?.result
            },
            async { feeCalculator.calculate() }
        )
        val account = getSequence.await() ?: throw Exception("No account found")
        val fee = getFee.await()

        SignerParams(
            params,
            XrpChainData(
                account.account_data?.Sequence ?: 0,
                account.ledger_current_index,
                fee,
            )
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    data class XrpChainData(
        val sequence: Int,
        val blockNumber: Int,
        val fee: Fee,
    ) : ChainSignData {
        override fun fee(speed: TxSpeed): Fee = fee
    }
}