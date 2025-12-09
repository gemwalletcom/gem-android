package com.gemwallet.android.data.service.store.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallet.core.primitives.PerpetualBalance

@Entity(tableName = "perpetual_balance")
data class DbPerpetualBalance(
    @PrimaryKey val accountAddress: String,
    val available: Double,
    val reserved: Double,
    val withdrawable: Double,
)

fun DbPerpetualBalance.toDTO(): PerpetualBalance {
    return PerpetualBalance(
        available,
        reserved,
        withdrawable
    )
}

fun PerpetualBalance.toDB(accountAddress: String): DbPerpetualBalance {
    return DbPerpetualBalance(
        accountAddress,
        available,
        reserved,
        withdrawable,
    )
}