package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerInputInfo
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class XrpSignerPreloader(
    private val chain: Chain,
    private val rpcClient: XrpRpcClient,
) : SignerPreload {
    override suspend fun invoke(owner: Account, params: ConfirmParams): Result<SignerParams> = withContext(Dispatchers.IO) {
        val (sequenceJob, feeJob) = Pair (
            async { rpcClient.account(owner.address) },
            async { XrpFee().invoke(chain, rpcClient) }
        )
        val (sequenceResult, fee) = Pair(sequenceJob.await(), feeJob.await())
        sequenceResult.mapCatching {
            val sequence = it.result.account_data.Sequence
            SignerParams(
                input = params,
                owner = owner.address,
                info = Info(
                    sequence = sequence,
                    fee = fee,
                )
            )

        }
    }

    override fun isMaintain(chain: Chain): Boolean = this.chain == chain

    data class Info(
        val sequence: Int,
        val fee: Fee,
    ) : SignerInputInfo {
        override fun fee(speed: TxSpeed): Fee = fee
    }
}