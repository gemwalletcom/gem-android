package com.gemwallet.features.bridge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.bridge.BridgesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionsViewModel @Inject constructor(
    private val bridgesRepository: BridgesRepository,
) : ViewModel() {

    val connections = bridgesRepository.getConnections()
        .stateIn(viewModelScope, SharingStarted.Companion.Lazily, emptyList())

    fun addPairing(uri: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            bridgesRepository.addPairing(
                uri = uri,
                onSuccess = onSuccess,
                onError = onError,/*{ msg -> state.update { it.copy(pairError = msg) } }*/
            )
        }
    }
}