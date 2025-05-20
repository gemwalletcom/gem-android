package com.gemwallet.android.features.bridge.connection.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.bridge.BridgesRepository
import com.gemwallet.android.features.bridge.navigation.connectionIdArg
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val bridgesRepository: BridgesRepository,
    savedState: SavedStateHandle
) : ViewModel() {

    private val request = savedState.getStateFlow(connectionIdArg, "")

    val connection = request.flatMapLatest { bridgesRepository.getConnections(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun disconnect(onSuccess: () -> Unit) {
        connection.value?.session?.id?.let {
            viewModelScope.launch(Dispatchers.IO) {
                bridgesRepository.disconnect(
                    id = it,
                    onSuccess = { viewModelScope.launch(Dispatchers.Main) { onSuccess() } },
                    onError = { viewModelScope.launch(Dispatchers.Main) { onSuccess() } },
                )
            }
        } ?: onSuccess()
    }
}