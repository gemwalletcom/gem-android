package com.gemwallet.android.features.recipient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.NameRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddressChainViewModel @Inject constructor(
    private val gemClient: GemApiClient,
) : ViewModel() {

    private var nameResolveJob: Job? = null
    private val state = MutableStateFlow(State())
    val uiState = state.stateIn(viewModelScope, SharingStarted.Eagerly, State())

    private var resolveListener: ((NameRecord?) -> Unit)? = null

    fun onNameRecord(chain: Chain?, nameRecord: String) {
        if (nameRecord.isEmpty()) {
            state.update { State() }
            return
        }
        val current = state.value.nameRecord
        if (nameRecord != current?.name) {
            onInput(nameRecord, chain)
        }
    }

    fun onInput(input: String, chain: Chain?) {
        if (nameResolveJob?.isActive == true) {
            nameResolveJob?.cancel()
        }
        state.update { State() }
        if (chain == null) {
            return
        }
        val subdomains = input.split(".")
        if (subdomains.size <= 1 || subdomains.lastOrNull().isNullOrEmpty()) {
            return
        }
        state.update { State(isLoading = true) }
        nameResolveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500L)
            val nameRecord = try {
                gemClient.resolve(input.lowercase(Locale.getDefault()), chain.string)
            } catch (_: Throwable) {
                null
            }
            setNameRecord(nameRecord, input)
        }
    }

    private fun setNameRecord(nameRecord: NameRecord?, input: String) {
        resolveListener?.invoke(nameRecord)
        val isResolve = !nameRecord?.address.isNullOrEmpty() && nameRecord.name.isNotEmpty()
        state.update {
            State(
                nameRecord = nameRecord,
                isLoading = false,
                isResolve = isResolve,
                isFail = !isResolve && input.isNotEmpty()
            )
        }
    }

    fun onResolved(onResolved: (NameRecord?) -> Unit) {
        this.resolveListener = onResolved
    }

    data class State(
        val isLoading: Boolean = false,
        val isResolve: Boolean = false,
        val isFail: Boolean = false,
        val nameRecord: NameRecord? = null,
    )
}