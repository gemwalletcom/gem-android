package com.gemwallet.android.features.activities.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.application.transactions.coordinators.GetTransactionDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    private val getTransactionDetails: GetTransactionDetails,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val data = savedStateHandle.getStateFlow<String?>("txId", null)
        .filterNotNull()
        .flatMapLatest { getTransactionDetails.getTransactionDetails(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}