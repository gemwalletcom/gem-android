package com.gemwallet.android.data.wallet

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "wallets")
data class WalletRoom(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "domain_name") val domainName: String?,
    val type: WalletType,
    val position: Int,
    val pinned: Boolean,
    val index: Int,
)

@Entity(tableName = "accounts", primaryKeys = ["wallet_id", "address", "chain", "derivation_path"])
data class AccountRoom(
    @ColumnInfo(name = "wallet_id") val walletId: String,
    @ColumnInfo(name = "derivation_path") val derivationPath: String,
    val address: String,
    val chain: Chain,
    val extendedPublicKey: String?,
)

@Dao
interface WalletsDao {
    @Query("SELECT * FROM wallets")
    fun getAll(): List<WalletRoom>

    @Query("SELECT * FROM wallets WHERE id = :id")
    fun getById(id: String): WalletRoom?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(wallet: WalletRoom)

    @Delete
    fun delete(account: WalletRoom)
}

@Dao
interface AccountsDao {
    @Query("SELECT * FROM accounts WHERE wallet_id = :walletId")
    fun getByWalletId(walletId: String): List<AccountRoom>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: AccountRoom)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: List<AccountRoom>)

    @Delete
    fun delete(account: AccountRoom)

    @Query("DELETE FROM accounts WHERE wallet_id=:walletId")
    fun deleteByWalletId(walletId: String)
}

@Singleton
class WalletsRoomSource @Inject constructor(
    private val walletsDao: WalletsDao,
    private val accountsDao: AccountsDao
) : WalletsLocalSource {

    override suspend fun getAll(): Result<List<Wallet>> = withContext(Dispatchers.IO) {
        val wallets = walletsDao.getAll()
        val result = wallets.map {  walletRoom ->
            val accounts = accountsDao.getByWalletId(walletRoom.id)
            Wallet(
                id = walletRoom.id,
                name = walletRoom.name,
                type =  walletRoom.type,
                accounts = accounts.toAccounts(),
                index = walletRoom.index,
            )
        }
        Result.success(result)
    }

    override suspend fun addWallet(wallet: Wallet): Result<Wallet> = withContext(Dispatchers.IO) {
        walletsDao.insert(
            WalletRoom(
                id = wallet.id,
                name = wallet.name,
                type = wallet.type,
                domainName = null,
                position = 0,
                pinned = false,
                index = wallet.index,
            )
        )
        wallet.accounts.forEach {
            accountsDao.insert(
                AccountRoom(
                    walletId = wallet.id,
                    derivationPath = it.derivationPath,
                    chain = it.chain,
                    address = it.address,
                    extendedPublicKey = it.extendedPublicKey,
                )
            )
        }
        Result.success(wallet)
    }

    override suspend fun updateWallet(wallet: Wallet): Result<Wallet> = addWallet(wallet)

    override suspend fun removeWallet(walletId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val wallet = walletsDao.getById(walletId)
        accountsDao.deleteByWalletId(wallet?.id
            ?: return@withContext Result.failure(Exception("Can't clean accounts")))
        walletsDao.delete(wallet)
        Result.success(true)
    }

    override suspend fun getWallet(walletId: String): Result<Wallet> = withContext(Dispatchers.IO) {
        val room = walletsDao.getById(walletId)
            ?: return@withContext Result.failure(Exception("Wallet doesn't exists"))
        val accounts = accountsDao.getByWalletId(walletId)
        if (accounts.isEmpty()) {
            return@withContext Result.failure(Exception("Wallet doesn't exists"))
        }
        Result.success(
            Wallet(
                id = room.id,
                name = room.name,
                type = room.type,
                accounts = accounts.toAccounts(),
                index = room.index,
            )
        )
    }

    private fun List<AccountRoom>.toAccounts() = map {
        Account(
            chain = it.chain,
            address = it.address,
            extendedPublicKey = it.extendedPublicKey,
            derivationPath = it.derivationPath,
        )
    }
}