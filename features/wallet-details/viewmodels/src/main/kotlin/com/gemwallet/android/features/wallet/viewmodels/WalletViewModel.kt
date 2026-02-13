package com.gemwallet.android.features.wallet.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.application.wallet.coordinators.GetWalletDetails
import com.gemwallet.android.application.wallet.coordinators.SetWalletName
import com.gemwallet.android.application.wallet.coordinators.DeleteWallet
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
    private val getWalletDetails: GetWalletDetails,
    private val setWalletName: SetWalletName,
    private val deleteWallet: DeleteWallet,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val wallet = savedStateHandle.getStateFlow<String?>("walletId", null)
        .flatMapLatest {
            if (it == null) return@flatMapLatest flowOf(null)
            getWalletDetails.getWallet(it)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setWalletName(name: String) = viewModelScope.launch(Dispatchers.IO) {
        setWalletName.setWalletName(wallet.value?.id ?: return@launch, name)
    }

    fun delete(onBoard: () -> Unit, onComplete: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        val walletId = wallet.value?.id ?: return@launch
        deleteWallet.deleteWallet(walletId, onBoard, onComplete)
    }
}