package com.gemwallet.android.model

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.BalanceType
import java.math.BigInteger

data class Balances(
    val items: List<AssetBalance> = emptyList()
) {
    fun calcTotal(): Crypto = Crypto(
        this.items.fold(BigInteger.ZERO) { acc, next -> if (next.balance.type == BalanceType.reserved)
            acc else acc.add(next.balance.value.toBigInteger())
        }
    )

    fun available() = Crypto(
        items.firstOrNull { it.balance.type == BalanceType.available }
            ?.balance?.value?.toBigInteger() ?: BigInteger.ZERO
    )

    fun rewards() = Crypto(
        items.firstOrNull { it.balance.type == BalanceType.rewards }
            ?.balance?.value?.toBigInteger() ?: BigInteger.ZERO
    )

    companion object {
        fun create(
            assetId: AssetId,
            available: BigInteger = BigInteger.ZERO,
            locked: BigInteger? = null,
            frozen: BigInteger? = null,
            staked: BigInteger? = null,
            pending: BigInteger? = null,
            rewards: BigInteger? = null,
            reserved: BigInteger? = null,
        ): Balances = Balances(
            items = mutableListOf<AssetBalance>().apply {
                add(AssetBalance(assetId, Balance(BalanceType.available, available.toString())))
                locked?.also { add(AssetBalance(assetId, Balance(BalanceType.locked, it.toString()))) }
                frozen?.also { add(AssetBalance(assetId, Balance(BalanceType.frozen, it.toString()))) }
                staked?.also { add(AssetBalance(assetId, Balance(BalanceType.staked, it.toString()))) }
                pending?.also { add(AssetBalance(assetId, Balance(BalanceType.pending, it.toString()))) }
                rewards?.also { add(AssetBalance(assetId, Balance(BalanceType.rewards, it.toString()))) }
                reserved?.also { add(AssetBalance(assetId, Balance(BalanceType.reserved, it.toString()))) }
            }
        )
    }
}