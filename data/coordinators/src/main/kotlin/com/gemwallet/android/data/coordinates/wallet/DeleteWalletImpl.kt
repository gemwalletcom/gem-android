package com.gemwallet.android.data.coordinates.wallet

import com.gemwallet.android.application.wallet.coordinators.DeleteWallet
import com.gemwallet.android.blockchain.operators.DeleteKeyStoreOperator
import com.gemwallet.android.cases.device.SyncSubscription
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteWalletImpl @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val walletsRepository: WalletsRepository,
    private val deleteKeyStoreOperator: DeleteKeyStoreOperator,
    private val syncSubscription: SyncSubscription,
) : DeleteWallet {

    override suspend fun deleteWallet(
        walletId: String,
        onBoard: () -> Unit,
        onComplete: () -> Unit
    ) = withContext(Dispatchers.IO) { // TODO: Switch context to sync device state and doesn't wait it. Find more correct solution
        val wallet = walletsRepository.getWallet(walletId).firstOrNull() ?: return@withContext
        val currentWalletId = sessionRepository.session().firstOrNull()?.wallet?.id

        if (!walletsRepository.removeWallet(walletId = walletId)) {
            return@withContext
        }
        if (wallet.type != WalletType.View) {
            if (!deleteKeyStoreOperator(walletId)) return@withContext
        }

        async {
            walletsRepository.getAll().firstOrNull()
                ?.let { syncSubscription.syncSubscription(it) }
        }

        val callback: () -> Unit = if (currentWalletId == walletId) {
            val nextWallet = walletsRepository.getAll().firstOrNull()
                ?.filter { it.id != walletId }
                ?.sortedBy { it.type }
                ?.minByOrNull { it.index }

            if (nextWallet == null) {
                sessionRepository.reset()
                onBoard
            } else {
                sessionRepository.setWallet(nextWallet)
                onComplete
            }
        } else {
            onComplete
        }

        withContext(Dispatchers.Main) {
            callback()
        }
    }
}