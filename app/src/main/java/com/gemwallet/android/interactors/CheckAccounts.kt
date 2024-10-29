package com.gemwallet.android.interactors

import com.gemwallet.android.blockchain.operators.CreateAccountOperator
import com.gemwallet.android.blockchain.operators.LoadPrivateDataOperator
import com.gemwallet.android.blockchain.operators.PasswordStore
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.repositories.chains.ChainInfoRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.wallet.WalletsRepository
import com.gemwallet.android.interactors.sync.SyncSubscription
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.WalletType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckAccounts @Inject constructor(
    private val gemApiClient: GemApiClient,
    private val walletsRepository: WalletsRepository,
    private val assetsRepository: AssetsRepository,
    private val loadPrivateDataOperator: LoadPrivateDataOperator,
    private val passwordStore: PasswordStore,
    private val createAccountOperator: CreateAccountOperator,
    private val configRepository: ConfigRepository,
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke() {
        val wallets = walletsRepository.getAll()
        wallets.forEach { wallet ->
            if (wallet.type != WalletType.multicoin) {
                return@forEach
            }
            val availableChains = assetsRepository.getNativeAssets(wallet).getOrNull()
                ?.map { it.id.chain }?.toSet() ?: emptySet()
            val newChains = getChainsToAdd(availableChains)
            if (newChains.isNotEmpty()) {
                val data = loadPrivateDataOperator(wallet, passwordStore.getPassword(wallet.id))
                val newAccounts = newChains.map { createAccountOperator(wallet.type, data, it) }
                val newWallet = wallet.copy(accounts = wallet.accounts + newAccounts)
                walletsRepository.updateWallet(newWallet)
                if (newAccounts.isNotEmpty()) {
                    assetsRepository.invalidateDefault(
                        newWallet,
                        sessionRepository.getSession()?.currency ?: Currency.USD
                    )
                }
                SyncSubscription(gemApiClient = gemApiClient, walletsRepository = walletsRepository, configRepository = configRepository).invoke()
            }
        }
    }

    private fun getChainsToAdd(available: Set<Chain>): List<Chain> {
        val allChains = Chain.entries.toList() - ChainInfoRepository.exclude.toSet()
        val toAdd = mutableListOf<Chain>()
        for (i in allChains) {
            if (!available.contains(i)) {
                toAdd.add(i)
            }
        }
        return toAdd
    }
}