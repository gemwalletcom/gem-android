package com.gemwallet.android.features.wallet.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.application.PasswordStore
import com.gemwallet.android.blockchain.operators.LoadPrivateDataOperator
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WalletSecretDataViewModel @Inject constructor(
    private val walletsRepository: WalletsRepository,
    private val passwordStore: PasswordStore,
    private val loadPrivateDataOperator: LoadPrivateDataOperator,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val walletId = savedStateHandle.getStateFlow<String?>("walletId", null)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val phrase = walletId.flatMapLatest {
        walletsRepository.getWallet(it ?: return@flatMapLatest flowOf(null))
    }
    .mapLatest { wallet ->
        try {
            wallet?.let {
                val password = passwordStore.getPassword(wallet.id)
                val phrase = loadPrivateDataOperator(wallet, password)
                phrase
            }
        } catch (_: Throwable) {
            null
        }?.split(" ") ?: emptyList()
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}