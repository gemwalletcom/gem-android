package com.gemwallet.android.model

import com.wallet.core.primitives.Asset
import java.math.BigInteger
import kotlin.String

data class AssetBalance(
    val asset: Asset,
    val balance: Balance<String> = Balance<String>("0", "0", "0", "0", "0", "0", "0"),
    val balanceAmount: Balance<Double> = Balance<Double>(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
    val totalAmount: Double = 0.0,
    val fiatTotalAmount: Double = 0.0,
) {
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
        ): AssetBalance {
            val balance = Balance(
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

fun Balance<Double>.getTotalAmount() = available + frozen + locked + staked + pending + rewards + reserved

fun Balance<Double>.getStackedAmount() = frozen + staked + pending + rewards + locked

fun Balance<String>.getTotalAmount() = BigInteger(available) +
        BigInteger(frozen) +
        BigInteger(locked) +
        BigInteger(staked) +
        BigInteger(pending) +
        BigInteger(rewards) +
        BigInteger(reserved)

fun Balance<String>.getStackedAmount() = BigInteger(frozen) +
        BigInteger(staked) +
        BigInteger(pending) +
        BigInteger(rewards) +
        BigInteger(locked)