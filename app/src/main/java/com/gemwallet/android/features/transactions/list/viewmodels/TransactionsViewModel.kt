package com.gemwallet.android.features.transactions.list.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.transaction.TransactionsRepository
import com.gemwallet.android.ext.mutableStateIn
import com.gemwallet.android.features.transactions.list.model.TxListScreenState
import com.gemwallet.android.interactors.sync.SyncTransactions
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.TransactionExtended
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    transactionsRepository: TransactionsRepository,
    private val syncTransactions: SyncTransactions,
) : ViewModel() {

    private val txState = transactionsRepository.getTransactions().map {
            State(
                loading = false,
                transactions = it,
                currency = sessionRepository.getSession()?.currency ?: Currency.USD
            )
        }
        .flowOn(Dispatchers.IO)
        .distinctUntilChanged()
        .mutableStateIn(viewModelScope, initialValue = State())

    val uiState = txState.map { it.toUIState() }
        .stateIn(viewModelScope, started = SharingStarted.Eagerly, TxListScreenState())

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch(Dispatchers.IO) {
        txState.update { it.copy(loading = true) }
        syncTransactions.invoke(sessionRepository.getSession()?.wallet?.index ?: return@launch)
        txState.update { it.copy(loading = false) }
    }

    private data class State(
        val loading: Boolean = false,
        val transactions: List<TransactionExtended> = emptyList(),
        val currency: Currency = Currency.USD,
    ) {
        fun toUIState(): TxListScreenState {
            return TxListScreenState(
                loading = loading,
                transactions = transactions,
            )
        }
    }
}

