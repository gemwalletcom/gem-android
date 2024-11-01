package com.gemwallet.android.data.service.store.database.mappers

import com.gemwallet.android.data.service.store.database.entities.DbAccount
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Wallet

class AccountMapper : Mapper<DbAccount, Account, Nothing, Wallet> {
    override fun asDomain(entity: DbAccount, options: (() -> Nothing)?): Account {
        return Account(
            chain = entity.chain,
            address = entity.address,
            extendedPublicKey = entity.extendedPublicKey,
            derivationPath = entity.derivationPath,
        )
    }

    override fun asEntity(domain: Account, options: (() -> Wallet)?): DbAccount {
        return DbAccount(
            walletId = options?.invoke()?.id ?: throw IllegalArgumentException(),
            derivationPath = domain.derivationPath,
            chain = domain.chain,
            address = domain.address,
            extendedPublicKey = domain.extendedPublicKey,
        )
    }
}