package com.gemwallet.android.features.activities.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.transactions.GetTransactions
import com.gemwallet.android.cases.transactions.SyncTransactions
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.mutableStateIn
import com.gemwallet.android.model.TransactionExtended
import com.wallet.core.primitives.Currency
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
    getTransactions: GetTransactions,
    private val syncTransactions: SyncTransactions,
) : ViewModel() {

    private val txState = getTransactions.getTransactions().map {
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
        syncTransactions.syncTransactions(sessionRepository.getSession()?.wallet ?: return@launch)
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

