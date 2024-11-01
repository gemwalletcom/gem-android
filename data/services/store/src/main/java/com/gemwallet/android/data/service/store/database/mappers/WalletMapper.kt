package com.gemwallet.android.data.service.store.database.mappers

import com.gemwallet.android.data.service.store.database.entities.DbAccount
import com.gemwallet.android.data.service.store.database.entities.DbWallet
import com.wallet.core.primitives.Wallet

class WalletMapper(private val accountMapper: AccountMapper) : Mapper<DbWallet, Wallet, List<DbAccount>, Nothing> {
    override fun asDomain(entity: DbWallet, options: (() -> List<DbAccount>)?): Wallet {
        return Wallet(
            id = entity.id,
            name = entity.name,
            type = entity.type,
            accounts = options?.invoke()?.map(accountMapper::asDomain) ?: emptyList(),
            index = entity.index,
            order = 0,
            isPinned = entity.pinned,
        )
    }

    override fun asEntity(domain: Wallet, options: (() -> Nothing)?): DbWallet {
        return DbWallet(
            id = domain.id,
            name = domain.name,
            type = domain.type,
            domainName = null,
            position = 0,
            pinned = domain.isPinned,
            index = domain.index,
        )
    }
}