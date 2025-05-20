package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet

@Entity(tableName = "session")
data class DbSession(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo("wallet_id") val walletId: String,
    val currency: String,
)

fun DbSession.toModel(wallet: Wallet): Session {
    return Session(
        wallet = wallet,
        currency = Currency.entries.firstOrNull { it.string == currency } ?: Currency.USD
    )
}

fun Session.toRecord(): DbSession {
    return DbSession(walletId = wallet.id, currency = currency.string)
}