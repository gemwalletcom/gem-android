package com.gemwallet.android.blockchain.clients.algorand

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.algorand.services.AlgorandAccountService
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getReserveBalance
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
        val amount = accountService.accounts(address).getOrNull()?.amount ?: return null
        val reserved = chain.getReserveBalance()
        val available = max(amount, reserved.toLong()).toString()
        return AssetBalance.create(chain.asset(), available = available, reserved = reserved.toString())
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}