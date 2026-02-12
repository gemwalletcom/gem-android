package com.gemwallet.android.features.wallet.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.wallet.DeleteWallet
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletsRepository: WalletsRepository,
    sessionRepository: SessionRepository,
    private val deleteWallet: DeleteWallet,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val walletId = savedStateHandle.getStateFlow<String?>("walletId", null)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val wallet = walletId.flatMapLatest { walletsRepository.getWallet(it ?: return@flatMapLatest flowOf(null)) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val session = sessionRepository.session()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setWalletName(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val wallet = wallet.value ?: return@launch
            walletsRepository.updateWallet(wallet.copy(name = name))
        }
    }

    fun delete(onBoard: () -> Unit, onComplete: () -> Unit) = viewModelScope.launch {
        deleteWallet.deleteWallet(session.value?.wallet?.id, wallet.value?.id ?: return@launch, onBoard, onComplete)
    }
}