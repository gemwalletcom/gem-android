package com.gemwallet.android.features.main.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.transactions.GetTransactions
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainScreenViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    getTransactions: GetTransactions
) : ViewModel() {
    val pendingTxCount = sessionRepository.session()
        .filterNotNull()
        .flatMapLatest { getTransactions.getPendingTransactionsCount() }
        .filterNotNull()
        .map { if (it == 0) null else it.toString() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}