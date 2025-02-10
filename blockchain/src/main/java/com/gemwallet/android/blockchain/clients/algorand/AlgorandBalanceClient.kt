package com.gemwallet.android.blockchain.clients.algorand

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.algorand.services.AlgorandAccountService
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Chain
import kotlin.math.max

class AlgorandBalanceClient(
    private val chain: Chain,
    private val accountService: AlgorandAccountService,
) : BalanceClient {

    override suspend fun getNativeBalance(
        chain: Chain,
        address: String
    ): AssetBalance? {
        val response = accountService.accounts(address).getOrNull() ?: return null
        val amount = response.amount

        val available = if (amount > 0L) {
            max(amount - response.min_balance, 0L).toString()
        } else {
            "0"
        }
        val reserved = if (response.amount > 0L) {
            response.min_balance.toString()
        } else {
            "0"
        }
        return AssetBalance.create(chain.asset(), available = available, reserved = reserved.toString())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}