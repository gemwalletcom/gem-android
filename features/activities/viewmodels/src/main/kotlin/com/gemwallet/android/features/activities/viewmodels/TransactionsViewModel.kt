package com.gemwallet.android.features.activities.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.transactions.GetTransactions
import com.gemwallet.android.cases.transactions.SyncTransactions
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.ext.chain
import com.gemwallet.android.ext.mutableStateIn
import com.gemwallet.android.model.TransactionExtended
import com.gemwallet.android.ui.models.TransactionTypeFilter
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
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

    val chainsFilter = MutableStateFlow<List<Chain>>(emptyList())
    val typeFilter = MutableStateFlow<List<TransactionTypeFilter>>(emptyList())

    private val txState = combine(
        chainsFilter,
        typeFilter,
        getTransactions.getTransactions(),
    ) { chainsFilter, typeFilter, transactions ->

        val transactions = transactions.filter { tx ->
            val byChain = if (chainsFilter.isEmpty()) {
                true
            } else {
                val txChains = (tx.assets + listOf(tx.asset, tx.feeAsset)).map { it.chain() }.toSet()
                chainsFilter.containsAll(txChains)
            }
            val byType = if (typeFilter.isEmpty()) {
                true
            } else {
                typeFilter.map { it.types }.flatten().contains(tx.transaction.type)
            }
            byChain && byType
        }

        State(
            loading = false,
            transactions = transactions,
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

    fun onChainFilter(chain: Chain) {
        chainsFilter.update {
            val chains = it.toMutableList()
            if (!chains.remove(chain)) {
                chains.add(chain)
            }
            chains.toList()
        }
    }

    fun onTypeFilter(type: TransactionTypeFilter) {
        typeFilter.update {
            val types = it.toMutableList()
            if (!types.remove(type)) {
                types.add(type)
            }
            types.toList()
        }
    }

    fun clearChainsFilter() {
        chainsFilter.update {
            emptyList()
        }
    }

    fun clearTypeFilter() {
        typeFilter.update {
            emptyList()
        }
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

