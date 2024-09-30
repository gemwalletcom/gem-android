package com.gemwallet.android.features.main.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.transactions.GetTransactionsCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    getTransactionsCase: GetTransactionsCase
) : ViewModel() {
    val pendingTxCount = getTransactionsCase.getPendingTransactions()
        .map { it.count() }
        .map { if (it == 0) null else it.toString() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}