package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetBalance
import com.gemwallet.android.model.Balance
import com.wallet.core.primitives.BalanceMetadata

@Entity(
    tableName = "balances",
    primaryKeys = ["asset_id", "wallet_id", "account_address"],
    foreignKeys = [
        ForeignKey(DbAsset::class, ["id"], ["asset_id"], onDelete = ForeignKey.Companion.CASCADE),
        ForeignKey(DbWallet::class, ["id"], ["wallet_id"], onDelete = ForeignKey.Companion.CASCADE),
    ],
)
data class DbBalance(
    @ColumnInfo("asset_id") val assetId: String,
    @ColumnInfo("wallet_id") val walletId: String,
    @ColumnInfo("account_address") val accountAddress: String,

    var available: String = "0",
    @ColumnInfo("available_amount") var availableAmount: Double = 0.0,

    var frozen: String = "0",
    @ColumnInfo("frozen_amount") var frozenAmount: Double = 0.0,

    var locked: String = "0",
    @ColumnInfo("locked_amount") var lockedAmount: Double = 0.0,

    var staked: String = "0",
    @ColumnInfo("staked_amount") var stakedAmount: Double = 0.0,

    var pending: String = "0",
    @ColumnInfo("pending_amount") var pendingAmount: Double = 0.0,

    var rewards: String = "0",
    @ColumnInfo("rewards_amount") var rewardsAmount: Double = 0.0,

    var reserved: String = "0",
    @ColumnInfo("reserved_amount") var reservedAmount: Double = 0.0,

    @ColumnInfo("total_amount") var totalAmount: Double = 0.0,
    @ColumnInfo("is_active") var isActive: Boolean = true,
    @ColumnInfo("votes", defaultValue = "0") var votes: Long = 0L,
    @ColumnInfo("energy_available", defaultValue = "0") var energyAvailable: Long = 0L,
    @ColumnInfo("energy_total", defaultValue = "0") var energyTotal: Long = 0L,
    @ColumnInfo("bandwidth_available", defaultValue = "0") var bandwidthAvailable: Long = 0L,
    @ColumnInfo("bandwidth_total", defaultValue = "0") var bandwidthTotal: Long = 0L,
    @ColumnInfo("updated_at") var updatedAt: Long?,
) {
    companion object
}

fun AssetBalance.toRecord(walletId: String, accountAddress: String, updateAt: Long): DbBalance {
    return DbBalance(
        assetId = this.asset.id.toIdentifier(),
        walletId = walletId,
        accountAddress = accountAddress,
        available = this.balance.available,
        availableAmount = this.balanceAmount.available,
        frozen = this.balance.frozen,
        frozenAmount = this.balanceAmount.frozen,
        locked = this.balance.locked,
        lockedAmount = this.balanceAmount.locked,
        staked = this.balance.staked,
        stakedAmount = this.balanceAmount.staked,
        pending = this.balance.pending,
        pendingAmount = this.balanceAmount.pending,
        rewards = this.balance.rewards,
        rewardsAmount = this.balanceAmount.rewards,
        reserved = this.balance.reserved,
        reservedAmount = this.balanceAmount.reserved,
        totalAmount = this.totalAmount,
        energyAvailable = this.metadata?.energyAvailable?.toLong() ?: 0L,
        energyTotal = this.metadata?.energyAvailable?.toLong() ?: 0L,
        bandwidthAvailable = this.metadata?.bandwidthAvailable?.toLong() ?: 0L,
        bandwidthTotal = this.metadata?.bandwidthTotal?.toLong() ?: 0L,
        votes = this.metadata?.votes?.toLong() ?: 0L,
        updatedAt = updateAt,
        isActive = isActive,
    )
}

fun DbBalance.toModel(): AssetBalance? {
    return AssetBalance(
        asset = assetId.toAssetId()?.chain?.asset() ?: return null,
        balance = Balance(
            available = available,
            frozen = frozen,
            locked = locked,
            staked = staked,
            pending = pending,
            rewards = rewards,
            reserved = reserved,
        ),
        balanceAmount = Balance(
            available = availableAmount,
            frozen = frozenAmount,
            locked = lockedAmount,
            staked = stakedAmount,
            pending = pendingAmount,
            rewards = rewardsAmount,
            reserved = reservedAmount,
        ),
        totalAmount = totalAmount,
        isActive = isActive,
        metadata = BalanceMetadata(
            votes = votes.toUInt(),
            energyAvailable = energyAvailable.toUInt(),
            energyTotal = energyTotal.toUInt(),
            bandwidthAvailable = bandwidthAvailable.toUInt(),
            bandwidthTotal = bandwidthTotal.toUInt(),
        )
    )
}

fun DbBalance.Companion.mergeNative(old: DbBalance?, current: DbBalance?): DbBalance? {
    old ?: return current
    current ?: return old

    val newBalance = old.copy(
        available = current.available,
        availableAmount = current.availableAmount
    )

    return newBalance.copy(
        totalAmount = newBalance.availableAmount
                + newBalance.frozenAmount
                + newBalance.lockedAmount
                + newBalance.stakedAmount
                + newBalance.pendingAmount
                + newBalance.rewardsAmount
    )
}

fun DbBalance.Companion.mergeDelegation(old: DbBalance?, current: DbBalance?): DbBalance? {
    old ?: return current
    current ?: return old

    val newBalance = current.copy(available = old.available, availableAmount = old.availableAmount)
    return newBalance.copy(
        totalAmount = newBalance.availableAmount
                + newBalance.frozenAmount
                + newBalance.lockedAmount
                + newBalance.stakedAmount
                + newBalance.pendingAmount
                + newBalance.rewardsAmount
                + newBalance.reservedAmount,

    )
}