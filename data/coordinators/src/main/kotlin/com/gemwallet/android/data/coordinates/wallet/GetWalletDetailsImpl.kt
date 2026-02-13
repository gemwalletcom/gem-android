package com.gemwallet.android.data.coordinates.wallet

import androidx.compose.runtime.Stable
import com.gemwallet.android.application.wallet.coordinators.GetWalletDetails
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.domains.wallet.aggregates.WalletDetailsAggregate
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class GetWalletDetailsImpl(
    private val walletsRepository: WalletsRepository
) : GetWalletDetails {

    override fun getWallet(walletId: String): Flow<WalletDetailsAggregate?> {
        return  walletsRepository.getWallet(walletId)
            .mapLatest { dto -> dto?.let { WalletDetailsAggregateImpl(it) } }
    }
}

@Stable
class WalletDetailsAggregateImpl(wallet: Wallet) : WalletDetailsAggregate {
    override val id: String = wallet.id
    override val name: String = wallet.name
    override val type: WalletType = wallet.type
    override val addresses: List<String> = wallet.accounts.map { it.address }
}