package com.gemwallet.android.model

data class Balance<T>(
    val available: T,
    val frozen: T,
    val locked: T,
    val staked: T,
    val pending: T,
    val rewards: T,
    val reserved: T,
) {
    override fun equals(other: Any?): Boolean {
        return other is Balance<*>
                && other.available == available
                && other.frozen == frozen
                && other.locked == locked
                && other.staked == staked
                && other.pending == pending
                && other.rewards == rewards
                && other.reserved == reserved
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + available.hashCode()
        result = 31 * result + frozen.hashCode()
        result = 31 * result + locked.hashCode()
        result = 31 * result + staked.hashCode()
        result = 31 * result + pending.hashCode()
        result = 31 * result + rewards.hashCode()
        result = 31 * result + reserved.hashCode()
        return result
    }
}