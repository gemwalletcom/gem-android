package com.gemwallet.android.data.service.store.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.FiatRate

@Entity(tableName = "currency_rates")
data class DbFiatRate(
    @PrimaryKey val currency: Currency,
    val rate: Double,
)

fun DbFiatRate.toDTO(): FiatRate {
    return FiatRate(currency.string, rate)
}

fun FiatRate.toRecord(): DbFiatRate? {
    return DbFiatRate(Currency.entries.firstOrNull { it.string == symbol } ?: return null, rate)
}

fun List<DbFiatRate>.toDTO() = map { it.toDTO() }

fun List<FiatRate>.toRecord() = mapNotNull { it.toRecord() }