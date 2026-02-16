package com.gemwallet.android.data.repositoreis.wallets

import com.gemwallet.android.application.wallet.coordinators.WalletIdGenerator
import com.gemwallet.android.blockchain.operators.CreateAccountOperator
import com.gemwallet.android.cases.wallet.ImportError
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
class WalletsRepositoryImpl @Inject constructor(
    private val walletsDao: WalletsDao,
    private val accountsDao: AccountsDao,
    private val createAccount: CreateAccountOperator,
    private val walletIdGenerator: WalletIdGenerator,
) : WalletsRepository {

    override suspend fun getNextWalletNumber(): Int {
        return getAll().map { it.size + 1 }.firstOrNull() ?: 0
    }

    override fun getAll(): Flow<List<Wallet>> = walletsDao.getAll().toDTO()

    override suspend fun addWatch(walletName: String, address: String, chain: Chain): Wallet {
        val walletId = walletIdGenerator.generateWalletId(WalletType.View, chain, address)
        val hasWallet = getWallet(walletId).firstOrNull()
        if (hasWallet != null) {
            throw ImportError.DuplicatedWallet(hasWallet)
        }

        return putWallet(
            Wallet(
                id = walletId,
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
    }

    override suspend fun addControlled(
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
        val walletId = walletIdGenerator.generateWalletId(type, priorityAccount!!.chain, priorityAccount.address)

        val hasWallet = getWallet(walletId).firstOrNull()
        if (hasWallet != null) {
            throw ImportError.DuplicatedWallet(hasWallet)
        }

        val wallet = Wallet(
            id = walletId,
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

    override suspend fun updateWallet(wallet: Wallet) {
        walletsDao.update(wallet.toRecord())
    }

    override suspend fun updateAccounts(wallet: Wallet) {
        accountsDao.insert(wallet.accounts.map { it.toRecord(wallet.id) })
    }

    override suspend fun removeWallet(walletId: String) = withContext(Dispatchers.IO) {
        val wallet = walletsDao.getById(walletId).firstOrNull() ?: return@withContext false
        accountsDao.deleteByWalletId(wallet.id)
        walletsDao.delete(wallet)
        true
    }

    override fun getWallet(walletId: String): Flow<Wallet?> {
        return walletsDao.getById(walletId).map { walletRecord ->
            val accounts = accountsDao.getByWalletId(walletId)
            if (accounts.isEmpty()) return@map null
            walletRecord?.toDTO(accounts)
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun putWallet(wallet: Wallet): Wallet = withContext(Dispatchers.IO) {
        walletsDao.insert(wallet.toRecord())
        accountsDao.insert(wallet.accounts.map { it.toRecord(wallet.id) })
        wallet
    }
}