package com.gemwallet.android.data.coordinates.wallet

import com.gemwallet.android.application.wallet.coordinators.ToggleWalletPin
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import kotlinx.coroutines.flow.firstOrNull

class ToggleWalletPinImpl(
    private val walletsRepository: WalletsRepository,
) : ToggleWalletPin {

    override suspend fun toggleWalletPin(walletId: String) {
        val wallet = walletsRepository.getWallet(walletId).firstOrNull() ?: return
        walletsRepository.updateWallet(wallet.copy(isPinned = !wallet.isPinned))
    }
}