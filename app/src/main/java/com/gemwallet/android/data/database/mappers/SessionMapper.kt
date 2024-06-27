package com.gemwallet.android.data.database.mappers

import com.gemwallet.android.data.database.entities.DbSession
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet

class SessionMapper(val wallet: Wallet) : Mapper<DbSession, Session> {
    override fun asDomain(entity: DbSession): Session = Session(
        wallet = wallet,
        currency = Currency.entries.firstOrNull { it.string == entity.currency } ?: Currency.USD
    )

    override fun asEntity(domain: Session): DbSession = DbSession(
        walletId = domain.wallet.id,
        currency = domain.currency.string
    )
}