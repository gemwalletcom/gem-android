package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.SignerPreload
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.SignerInputInfo
import com.gemwallet.android.model.SignerParams
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.blockchain.bitcoin.models.BitcoinUTXO
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain

class BitcoinSignerPreloader(
    private val chain: Chain,
    private val rpcClient: BitcoinRpcClient,
) : SignerPreload {

    override suspend fun invoke(
        owner: Account,
        params: ConfirmParams,
    ): Result<SignerParams> {
        return rpcClient.getUTXO(owner.extendedPublicKey!!).mapCatching {
            val fee = BitcoinFee().invoke(rpcClient, it, owner, params.destination()?.address!!, params.amount)
            SignerParams(
                input = params,
                owner = owner.address,
                info = Info(it, fee)
            )
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    data class Info(
        val utxo: List<BitcoinUTXO>,
        val fee: List<Fee>,
    ) : SignerInputInfo {
        override fun fee(speed: TxSpeed): Fee = fee.firstOrNull { it.speed == speed } ?: fee.first()

        override fun allFee(): List<Fee> = fee
    }
}