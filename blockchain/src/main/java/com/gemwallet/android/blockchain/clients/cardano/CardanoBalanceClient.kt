package com.gemwallet.android.blockchain.clients.cardano

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.cardano.services.CardanoBalanceService
import com.gemwallet.android.blockchain.clients.cardano.services.balance
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Chain

class CardanoBalanceClient(
    private val chain: Chain,
    private val balanceService: CardanoBalanceService,
) : BalanceClient {

    override suspend fun getNativeBalance(
        chain: Chain,
        address: String
    ): AssetBalance? {
        val amount = try {
            balanceService.balance(address)
        } catch (_: Throwable) {
            null
        } ?: return null
        return AssetBalance.create(chain.asset(), available = amount.toString())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}