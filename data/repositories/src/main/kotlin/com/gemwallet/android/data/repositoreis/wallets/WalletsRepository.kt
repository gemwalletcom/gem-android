package com.gemwallet.android.data.repositoreis.wallets

import com.gemwallet.android.application.wallet.coordinators.WalletIdGenerator
import com.gemwallet.android.blockchain.operators.CreateAccountOperator
import com.gemwallet.android.data.service.store.database.AccountsDao
import com.gemwallet.android.data.service.store.database.WalletsDao
import com.gemwallet.android.data.service.store.database.entities.toDTO
import com.gemwallet.android.data.service.store.database.entities.toRecord
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletSource
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletsRepository @Inject constructor(
    private val walletsDao: WalletsDao,
    private val accountsDao: AccountsDao,
    private val createAccount: CreateAccountOperator,
    private val walletIdGenerator: WalletIdGenerator,
) {

    suspend fun getNextWalletNumber(): Int {
        return getAll().map { it.size + 1 }.firstOrNull() ?: 0
    }

    fun getAll() = walletsDao.getAll().toDTO()

    suspend fun addWatch(walletName: String, address: String, chain: Chain): Wallet =
        putWallet(
            Wallet(
                id = walletIdGenerator.generateWalletId(WalletType.View, chain, address),
                name = walletName,
                type = WalletType.View,
                accounts = listOf(
                    Account(
                        address = address,
                        chain = chain,
                        derivationPath = "",
                    )
                ),
                index = getNextWalletNumber(),
                order = 0,
                isPinned = false,
                source = WalletSource.Import,
            )
        )

    suspend fun addControlled(
        walletName: String,
        data: String,
        type: WalletType,
        chain: Chain?,
        source: WalletSource
    ): Wallet {
        val accounts = mutableListOf<Account>()
        val chains =
            if ((type == WalletType.Single || type == WalletType.PrivateKey) && chain != null) listOf(
                chain
            ) else Chain.entries
        for (item in chains) {
            accounts.add(createAccount(type, data, item))
        }
        val priorityAccount = walletIdGenerator.getPriorityAccount(accounts)
        val wallet = Wallet(
            id = walletIdGenerator.generateWalletId(type, priorityAccount!!.chain, priorityAccount.address),
            name = walletName,
            type = type,
            accounts = accounts,
            index = getNextWalletNumber(),
            order = 0,
            isPinned = false,
            source = source,
        )
        return putWallet(wallet)
    }

    suspend fun updateWallet(wallet: Wallet) {
        walletsDao.update(wallet.toRecord())
    }

    suspend fun updateAccounts(wallet: Wallet) {
        accountsDao.insert(wallet.accounts.map { it.toRecord(wallet.id) })
    }

    suspend fun removeWallet(walletId: String) = withContext(Dispatchers.IO) {
        val wallet = walletsDao.getById(walletId).firstOrNull() ?: return@withContext false
        accountsDao.deleteByWalletId(wallet.id)
        walletsDao.delete(wallet)
        true
    }

    fun getWallet(walletId: String): Flow<Wallet?> {
        return walletsDao.getById(walletId).map { walletRecord ->
            val accounts = accountsDao.getByWalletId(walletId)
            if (accounts.isEmpty()) return@map null
            walletRecord?.toDTO(accounts)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun togglePin(walletId: String) = withContext(Dispatchers.IO) {
        val room = walletsDao.getById(walletId).firstOrNull() ?: return@withContext
        walletsDao.update(room.copy(pinned = !room.pinned))
    }

    suspend fun putWallet(wallet: Wallet): Wallet = withContext(Dispatchers.IO) {
        walletsDao.insert(wallet.toRecord())
        accountsDao.insert(wallet.accounts.map { it.toRecord(wallet.id) })
        wallet
    }
}