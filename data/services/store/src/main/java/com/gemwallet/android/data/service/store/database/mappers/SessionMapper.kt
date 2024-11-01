package com.gemwallet.android.data.service.store.database.mappers

import com.gemwallet.android.data.service.store.database.entities.DbSession
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet

class SessionMapper : Mapper<DbSession, Session, Wallet, Nothing> {

    override fun asDomain(entity: DbSession, options: (() -> Wallet)?): Session = Session(
        wallet = options?.invoke() ?: throw IllegalArgumentException(),
        currency = Currency.entries.firstOrNull { it.string == entity.currency } ?: Currency.USD
    )

    override fun asEntity(domain: Session, options: (() -> Nothing)?): DbSession = DbSession(
        walletId = domain.wallet.id,
        currency = domain.currency.string
    )
}