package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerInputInfo
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain

class AptosSignerPreloader(
    private val chain: Chain,
    private val rpcClient: AptosRpcClient,
) : SignerPreload {
    override suspend fun invoke(
        owner: Account,
        params: ConfirmParams,
    ): Result<SignerParams> {
        val sequence = try {
            rpcClient.accounts(owner.address).getOrThrow().sequence_number?.toLong() ?: 0L
        } catch (err: Throwable) {
            0
        }
        val fee = AptosFee().invoke(chain, params.destination()?.address!!, rpcClient)
        val input = SignerParams(
            input = params,
            owner = owner.address,
            info = Info(sequence = sequence, fee)
        )
        return Result.success(input)
    }

    override fun maintainChain(): Chain = chain

    data class Info(
        val sequence: Long,
        val fee: Fee,
    ) : SignerInputInfo {
        override fun fee(speed: TxSpeed): Fee = fee

    }
}