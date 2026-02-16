package com.gemwallet.android.data.coordinates.wallet

import androidx.compose.runtime.Stable
import com.gemwallet.android.application.wallet.coordinators.GetAllWallets
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.domains.wallet.aggregates.WalletDataAggregate
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class GetAllWalletsImpl(
    private val sessionRepository: SessionRepository,
    private val walletsRepository: WalletsRepository,
) : GetAllWallets {

    override fun getAllWallets(): Flow<List<WalletDataAggregate>> {
        return sessionRepository.session().flatMapLatest { session ->
            val currentWalletId = session?.wallet?.id
            walletsRepository.getAll().map { items ->
                val watch = items.filter { it.type == WalletType.View }
                val single = items.filter { it.type == WalletType.Single }
                val privateKey = items.filter { it.type == WalletType.PrivateKey }
                val multi = items.filter { it.type == WalletType.Multicoin }
                multi + single + privateKey + watch
            }.mapLatest { items ->
                items.map { WalletDataAggregateImpl(it, it.id == currentWalletId) }
            }
        }
    }
}

@Stable
class WalletDataAggregateImpl(
    private val wallet: Wallet,
    override val isCurrent: Boolean
) : WalletDataAggregate {

    override val id: String = wallet.id

    override val name: String = wallet.name

    override val type: WalletType = wallet.type

    override val walletChain: Chain? = walletAccount?.chain

    override val walletAddress: String? = walletAccount?.address

    override val isPinned: Boolean = wallet.isPinned

    private val walletAccount: Account?
        get() = when (type) {
            WalletType.View,
            WalletType.PrivateKey,
            WalletType.Single -> wallet.accounts.firstOrNull()
            WalletType.Multicoin -> wallet.accounts.firstOrNull { it.chain == Chain.Ethereum }
        }
}