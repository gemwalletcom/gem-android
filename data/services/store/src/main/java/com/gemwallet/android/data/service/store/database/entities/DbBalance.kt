package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetBalance
import com.gemwallet.android.model.Balance

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
    @ColumnInfo("updated_at") var updatedAt: Long?
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
        updatedAt = updateAt,
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
                + newBalance.reservedAmount
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
                + newBalance.reservedAmount
    )
}