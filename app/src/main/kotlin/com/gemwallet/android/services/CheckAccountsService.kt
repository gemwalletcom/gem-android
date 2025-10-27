package com.gemwallet.android.services

import com.gemwallet.android.blockchain.operators.CreateAccountOperator
import com.gemwallet.android.blockchain.operators.LoadPrivateDataOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.cases.device.SyncSubscription
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.ext.available
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckAccountsService @Inject constructor(
    private val walletsRepository: WalletsRepository,
    private val assetsRepository: AssetsRepository,
    private val loadPrivateDataOperator: LoadPrivateDataOperator,
    private val passwordStore: PasswordStore,
    private val createAccountOperator: CreateAccountOperator,
    private val sessionRepository: SessionRepository,
    private val syncSubscription: SyncSubscription,
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val wallets = walletsRepository.getAll().firstOrNull() ?: emptyList()

        wallets.forEach { wallet ->
            val nativeAssets = assetsRepository.getNativeAssets(wallet)

            if (wallet.type != WalletType.multicoin) {
                if (nativeAssets.isEmpty()) {
                    assetsRepository.invalidateDefault(wallet)
                }
                return@forEach
            }

            val availableChains = nativeAssets.map { it.id.chain }.toSet()
            val newChains = getChainsToAdd(availableChains, wallet.accounts)

            if (newChains.isNotEmpty()) {
                val data = loadPrivateDataOperator(wallet, passwordStore.getPassword(wallet.id))
                val newAccounts = newChains.map { createAccountOperator(wallet.type, data, it) }
                val newWallet = wallet.copy(accounts = wallet.accounts + newAccounts)
                walletsRepository.updateWallet(newWallet)
                walletsRepository.updateAccounts(newWallet)
                if (newAccounts.isNotEmpty()) {
                    assetsRepository.invalidateDefault(newWallet)
                }
                syncSubscription.syncSubscription(walletsRepository.getAll().firstOrNull() ?: emptyList())
            }
        }
    }

    private fun getChainsToAdd(available: Set<Chain>, accounts: List<Account>): List<Chain> {
        val allChains = Chain.available().toSet()
        val accountChains = accounts.map { it.chain }
        val toAdd = mutableListOf<Chain>()
        for (i in allChains) {
            if (!available.contains(i) || !accountChains.contains(i)) {
                toAdd.add(i)
            }
        }
        return toAdd
    }
}