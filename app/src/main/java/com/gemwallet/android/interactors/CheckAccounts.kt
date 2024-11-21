package com.gemwallet.android.interactors

import com.gemwallet.android.blockchain.operators.CreateAccountOperator
import com.gemwallet.android.blockchain.operators.LoadPrivateDataOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.cases.device.SyncSubscriptionCase
import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.ext.available
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.WalletType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckAccounts @Inject constructor(
    private val walletsRepository: WalletsRepository,
    private val assetsRepository: AssetsRepository,
    private val loadPrivateDataOperator: LoadPrivateDataOperator,
    private val passwordStore: PasswordStore,
    private val createAccountOperator: CreateAccountOperator,
    private val sessionRepository: SessionRepository,
    private val syncSubscriptionCase: SyncSubscriptionCase,
) {
    suspend operator fun invoke() {
        val wallets = walletsRepository.getAll()
        val currency = sessionRepository.getSession()?.currency ?: Currency.USD

        wallets.forEach { wallet ->
            val nativeAssets = assetsRepository.getNativeAssets(wallet)

            if (wallet.type != WalletType.multicoin) {
                if (nativeAssets.isEmpty()) {
                    assetsRepository.invalidateDefault(wallet, currency)
                }
                return@forEach
            }

            val availableChains = nativeAssets.map { it.id().chain }.toSet()
            val newChains = getChainsToAdd(availableChains)

            if (newChains.isNotEmpty()) {
                val data = loadPrivateDataOperator(wallet, passwordStore.getPassword(wallet.id))
                val newAccounts = newChains.map { createAccountOperator(wallet.type, data, it) }
                val newWallet = wallet.copy(accounts = wallet.accounts + newAccounts)
                walletsRepository.updateWallet(newWallet)
                if (newAccounts.isNotEmpty()) {
                    assetsRepository.invalidateDefault(newWallet, currency)
                }
                syncSubscriptionCase.syncSubscription(walletsRepository.getAll())
            }
        }
    }

    private fun getChainsToAdd(available: Set<Chain>): List<Chain> {
        val allChains = Chain.available().toSet()
        val toAdd = mutableListOf<Chain>()
        for (i in allChains) {
            if (!available.contains(i)) {
                toAdd.add(i)
            }
        }
        return toAdd
    }
}