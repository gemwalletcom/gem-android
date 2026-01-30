package com.gemwallet.android.data.coordinates.perpetuals

import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualBalances
import com.gemwallet.android.data.repositoreis.perpetual.PerpetualRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.model.format
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PerpetualBalance
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class GetPerpetualBalancesImpl(
    private val sessionRepository: SessionRepository,
    private val perpetualRepository: PerpetualRepository,
) : GetPerpetualBalances {

    override fun getPerpetualBalance(): Flow<com.gemwallet.android.domains.perpetual.values.PerpetualBalance> {
        return sessionRepository.session().map { it?.wallet?.accounts?.map { it.address } ?: emptyList()}
            .filter { it.isNotEmpty() }
            .flatMapLatest { perpetualRepository.getBalances(it) }
            .map { items ->
                items.fold(
                    PerpetualBalance(0.0, 0.0, 0.0), { acc, item ->
                        PerpetualBalance(
                            available = acc.available + item.available,
                            reserved = acc.reserved + item.reserved,
                            withdrawable = acc.withdrawable + item.withdrawable,
                        )
                    }
                )
            }
            .map { PerpetualBalanceImpl(it) }
    }
}

class PerpetualBalanceImpl(
    val balance: PerpetualBalance,
) : com.gemwallet.android.domains.perpetual.values.PerpetualBalance {
    override val deposit: String
        get() = Currency.USD.format(balance.reserved)

    override val available: String
        get() = Currency.USD.format(balance.available)

    override val withdrawable: String
        get() = Currency.USD.format(balance.withdrawable)

    override val total: String
        get() = Currency.USD.format(balance.reserved + balance.available + balance.withdrawable)
}