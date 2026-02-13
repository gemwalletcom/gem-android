package com.gemwallet.android.features.wallet.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.application.wallet.coordinators.GetWalletSecretData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WalletSecretDataViewModel @Inject constructor(
    private val getWalletSecretData: GetWalletSecretData,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val data = savedStateHandle.getStateFlow<String?>("walletId", null).flatMapLatest {
        it ?: return@flatMapLatest flowOf(null)
        getWalletSecretData.getSecretData(it)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}