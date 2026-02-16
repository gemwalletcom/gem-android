package com.gemwallet.android.data.coordinates.wallet

import com.gemwallet.android.application.wallet.coordinators.SetCurrentWallet
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import kotlinx.coroutines.flow.firstOrNull

class SetCurrentWalletImpl(
    private val sessionRepository: SessionRepository,
    private val walletsRepository: WalletsRepository,
) : SetCurrentWallet {

    override suspend fun setCurrentWallet(walletId: String) {
        val wallet = walletsRepository.getWallet(walletId).firstOrNull() ?: return
        sessionRepository.setWallet(wallet)
    }
}