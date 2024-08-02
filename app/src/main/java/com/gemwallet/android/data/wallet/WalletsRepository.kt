package com.gemwallet.android.data.wallet

import com.gemwallet.android.blockchain.operators.CreateAccountOperator
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletsRepository @Inject constructor(
    private val walletsLocalSource: WalletsLocalSource,
    private val createAccount: CreateAccountOperator,
) {
    suspend fun getNextWalletNumber(): Int {
        return (walletsLocalSource.getAll().getOrNull()?.size ?: 0) + 1
    }

    suspend fun getAll() = walletsLocalSource.getAll()

    suspend fun addWatch(walletName: String, address: String, chain: Chain): Result<Wallet> =
        walletsLocalSource.addWallet(
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
            )
        )

    suspend fun addControlled(walletName: String, data: String, type: WalletType, chain: Chain?): Result<Wallet> {
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
        )
        return walletsLocalSource.addWallet(wallet)
    }

    suspend fun updateWallet(wallet: Wallet) {
        walletsLocalSource.updateWallet(wallet)
    }

    suspend fun removeWallet(walletId: String) = walletsLocalSource.removeWallet(walletId)

    suspend fun getWallet(walletId: String): Result<Wallet> = walletsLocalSource.getWallet(walletId)

    private fun newWalletId() = UUID.randomUUID().toString()
}