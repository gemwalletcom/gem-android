package com.gemwallet.android.data.repositoreis.wallets

import com.gemwallet.android.blockchain.operators.CreateAccountOperator
import com.gemwallet.android.data.service.store.database.AccountsDao
import com.gemwallet.android.data.service.store.database.WalletsDao
import com.gemwallet.android.data.service.store.database.entities.toModel
import com.gemwallet.android.data.service.store.database.entities.toRecord
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
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

    suspend fun getNextWalletNumber(): Int {
        return getAll().map { it.size + 1 }.firstOrNull() ?: 0
    }

    fun getAll() = walletsDao.getAll().toModel()

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
        walletsDao.update(wallet.toRecord())
    }

    suspend fun removeWallet(walletId: String) = withContext(Dispatchers.IO) {
        val wallet = walletsDao.getById(walletId).firstOrNull() ?: return@withContext false
        accountsDao.deleteByWalletId(wallet.id)
        walletsDao.delete(wallet)
        true
    }

    suspend fun getWallet(walletId: String): Wallet? = withContext(Dispatchers.IO) {
        walletsDao.getById(walletId).map { walletRecord ->
            val accounts = accountsDao.getByWalletId(walletId)
            if (accounts.isEmpty()) return@map null
            walletRecord?.toModel(accounts)
        }.firstOrNull()
    }

    suspend fun togglePin(walletId: String) = withContext(Dispatchers.IO) {
        val room = walletsDao.getById(walletId).firstOrNull() ?: return@withContext
        walletsDao.insert(room.copy(pinned = !room.pinned))
    }

    suspend fun putWallet(wallet: Wallet): Wallet = withContext(Dispatchers.IO) {
        walletsDao.insert(wallet.toRecord())
        accountsDao.insert(wallet.accounts.map { it.toRecord(wallet.id) })
        wallet
    }

    private fun newWalletId() = UUID.randomUUID().toString()
}