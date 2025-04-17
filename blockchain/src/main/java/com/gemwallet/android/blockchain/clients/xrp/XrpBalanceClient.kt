package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.xrp.services.XrpAccountsService
import com.gemwallet.android.blockchain.clients.xrp.services.account
import com.gemwallet.android.blockchain.clients.xrp.services.accountLines
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.getReserveBalance
import com.gemwallet.android.model.AssetBalance
import com.gemwallet.android.model.Crypto
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain

class XrpBalanceClient(
    private val chain: Chain,
    private val accountsService: XrpAccountsService,
) : BalanceClient {

    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance? {
        val reserved = chain.getReserveBalance()
        val amount = accountsService.account(address).mapCatching {
            it.result.account_data?.Balance?.toBigInteger()?.let { it - reserved }
        }.getOrNull() ?: return null
        return AssetBalance.create(
            chain.asset(),
            available = amount.toString(),
            reserved = reserved.toString()
        )
    }

    override suspend fun getTokenBalances(
        chain: Chain,
        address: String,
        tokens: List<Asset>
    ): List<AssetBalance> {
        val lines = accountsService.accountLines(address).getOrNull()?.result?.lines ?: emptyList()
        return tokens.map { token ->
            val line = lines.firstOrNull { it.account == token.id.tokenId && it.currency.length == 40 }

            if (line == null) {
                AssetBalance.create(token, available = "0", isActive = false)
            } else {
                val value = Crypto(line.balance.toBigDecimal(), token.decimals).atomicValue.toString()
                AssetBalance.create(token, available = value, isActive = true)
            }
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

}