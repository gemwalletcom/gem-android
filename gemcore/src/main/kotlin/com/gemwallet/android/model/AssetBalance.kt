package com.gemwallet.android.model

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.BalanceMetadata
import java.math.BigInteger

data class AssetBalance(
    val asset: Asset,
    val balance: Balance<String> = Balance("0", "0", "0", "0", "0", "0", "0"),
    val balanceAmount: Balance<Double> = Balance(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
    val totalAmount: Double = 0.0,
    val fiatTotalAmount: Double = 0.0,
    val metadata: BalanceMetadata? = null,
    val isActive: Boolean = true,
) {

    override fun equals(other: Any?): Boolean {
        return other is AssetBalance
                && asset.id == other.asset.id
                && balance == other.balance
                && totalAmount == other.totalAmount
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + asset.hashCode()
        result = 31 * result + balance.hashCode()
        result = 31 * result + balanceAmount.hashCode()
        result = 31 * result + totalAmount.hashCode()
        result = 31 * result + fiatTotalAmount.hashCode()
        result = 31 * result + isActive.hashCode()
        return result
    }

    companion object {
        fun create(
            asset: Asset,
            available: String = "0",
            frozen: String = "0",
            locked: String = "0",
            staked: String = "0",
            pending: String = "0",
            rewards: String = "0",
            reserved: String = "0",
            metadata: BalanceMetadata? = null,
            isActive: Boolean = true,
        ): AssetBalance {
            val balance = Balance(  // TODO: Check number is correct
                available = available,
                frozen = frozen,
                locked = locked,
                staked = staked,
                pending = pending,
                rewards = rewards,
                reserved = reserved,
            )
            val balanceAmount = balance.createAmount(asset.decimals)
            return AssetBalance(
                asset = asset,
                balance = balance,
                balanceAmount = balanceAmount,
                totalAmount = balanceAmount.getTotalAmount(),
                fiatTotalAmount = 0.0,
                metadata = metadata,
                isActive = isActive,
            )
        }
    }
}


private fun Balance<String>.createAmount(decimals: Int) = Balance<Double>(
    available = Crypto(available).value(decimals).stripTrailingZeros().toDouble(),
    frozen = Crypto(frozen).value(decimals).stripTrailingZeros().toDouble(),
    locked = Crypto(locked).value(decimals).stripTrailingZeros().toDouble(),
    staked = Crypto(staked).value(decimals).stripTrailingZeros().toDouble(),
    pending = Crypto(pending).value(decimals).stripTrailingZeros().toDouble(),
    rewards = Crypto(rewards).value(decimals).stripTrailingZeros().toDouble(),
    reserved = Crypto(reserved).value(decimals).stripTrailingZeros().toDouble(),
)

fun Balance<String>.hasAvailable() = try {
    available.toBigInteger() > BigInteger.ZERO
} catch (_: Throwable) {
    false
}

fun Balance<Double>.getTotalAmount() = available + frozen + locked + staked + pending + rewards

fun Balance<Double>.getStackedAmount() = frozen + staked + pending + rewards + locked

fun Balance<String>.getTotalAmount() = BigInteger(available) +
        BigInteger(frozen) +
        BigInteger(locked) +
        BigInteger(staked) +
        BigInteger(pending) +
        BigInteger(rewards)

fun Balance<String>.getStackedAmount() = BigInteger(frozen) +
        BigInteger(staked) +
        BigInteger(pending) +
        BigInteger(rewards) +
        BigInteger(locked)