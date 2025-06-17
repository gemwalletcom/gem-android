package com.gemwallet.android.data.repositoreis.wallets

import com.gemwallet.android.blockchain.operators.DeleteKeyStoreOperator
import com.gemwallet.android.cases.wallet.DeleteWallet
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteWalletOperator @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val walletsRepository: WalletsRepository,
    private val deleteKeyStoreOperator: DeleteKeyStoreOperator,
) : DeleteWallet {

    override suspend fun deleteWallet(walletId: String, onBoard: () -> Unit, onComplete: () -> Unit) =
        withContext(Dispatchers.IO) {
            val session = sessionRepository.getSession() ?: return@withContext
            val wallet = walletsRepository.getWallet(walletId).firstOrNull() ?: return@withContext
            if (!walletsRepository.removeWallet(walletId = walletId)) {
                return@withContext
            }
            if (wallet.type == WalletType.multicoin) {
                if (!deleteKeyStoreOperator(walletId)) {
                    return@withContext
                }
            }
            if (session.wallet.id == walletId) {
                val wallets = walletsRepository.getAll().firstOrNull() ?: emptyList()
                if (wallets.isEmpty()) {
                    sessionRepository.reset()
                    withContext(Dispatchers.Main) {
                        onBoard()
                    }
                } else {
                    sessionRepository.setWallet(wallets.first())
                }
            }
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
}