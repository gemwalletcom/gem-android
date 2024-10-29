package com.gemwallet.android.data.wallet

import com.gemwallet.android.blockchain.operators.CreateAccountOperator
import com.gemwallet.android.data.database.AccountsDao
import com.gemwallet.android.data.database.WalletsDao
import com.gemwallet.android.data.database.mappers.AccountMapper
import com.gemwallet.android.data.database.mappers.WalletMapper
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletsRepository @Inject constructor(
    private val walletsDao: WalletsDao,
    private val accountsDao: AccountsDao,
    private val createAccount: CreateAccountOperator,
) {
    private val accountMapper = AccountMapper()
    private val walletMapper = WalletMapper(accountMapper)

    suspend fun getNextWalletNumber(): Int {
        return getAll().size + 1
    }

    suspend fun getAll() = withContext(Dispatchers.IO) {
        walletsDao.getAll().map {  entity ->
            walletMapper.asDomain(entity) { accountsDao.getByWalletId(entity.id) }
        }
    }

    suspend fun addWatch(walletName: String, address: String, chain: Chain): Wallet =
        putWallet(
            Wallet(
                id = newWalletId(),
                name = walletName,
                type = WalletType.view,
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
            )
        )

    suspend fun addControlled(walletName: String, data: String, type: WalletType, chain: Chain?): Wallet {
        val accounts = mutableListOf<Account>()
        val chains = if ((type == WalletType.single || type == WalletType.private_key) && chain != null) listOf(chain) else Chain.entries
        for (item in chains) {
            accounts.add(createAccount(type, data, item))
        }
        val wallet = Wallet(
            id = newWalletId(),
            name = walletName,
            type = type,
            accounts = accounts,
            index = getNextWalletNumber(),
            order = 0,
            isPinned = false,
        )
        return putWallet(wallet)
    }

    suspend fun updateWallet(wallet: Wallet) {
        putWallet(wallet)
    }

    suspend fun removeWallet(walletId: String) = withContext(Dispatchers.IO) {
        val wallet = walletsDao.getById(walletId) ?: return@withContext false
        accountsDao.deleteByWalletId(wallet.id)
        walletsDao.delete(wallet)
        true
    }

    suspend fun getWallet(walletId: String): Wallet? = withContext(Dispatchers.IO) {
        val room = walletsDao.getById(walletId) ?: return@withContext null
        val accounts = accountsDao.getByWalletId(walletId)
        if (accounts.isEmpty()) {
            return@withContext null
        }
        walletMapper.asDomain(room) { accounts }
    }

    suspend fun togglePin(walletId: String) = withContext(Dispatchers.IO) {
        val room = walletsDao.getById(walletId) ?: return@withContext
        walletsDao.insert(room.copy(pinned = !room.pinned))
    }

    suspend fun putWallet(wallet: Wallet): Wallet = withContext(Dispatchers.IO) {
        walletsDao.insert(walletMapper.asEntity(wallet))
        wallet.accounts.forEach {
            accountsDao.insert(accountMapper.asEntity(it) { wallet })
        }
        wallet
    }

    private fun newWalletId() = UUID.randomUUID().toString()
}