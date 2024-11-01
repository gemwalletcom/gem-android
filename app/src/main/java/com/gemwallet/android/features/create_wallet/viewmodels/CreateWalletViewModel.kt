package com.gemwallet.android.features.create_wallet.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.operators.CreateWalletOperator
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.features.import_wallet.viewmodels.ImportType
import com.gemwallet.android.interactors.ImportWalletOperator
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CreateWalletViewModel @Inject constructor(
    private val createWalletOperator: CreateWalletOperator,
    private val walletsRepository: WalletsRepository,
    private val importWalletOperator: ImportWalletOperator,
) : ViewModel() {

    private val state = MutableStateFlow(CreateWalletViewModelState())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CreateWalletUIState())

    init {
        viewModelScope.launch {
            val generatedNameIndex = walletsRepository.getNextWalletNumber()
            state.update { it.copy(generatedNameIndex = generatedNameIndex) }
            createWalletOperator()
                .onSuccess { data ->
                    state.update { it.copy(data = data.split(" ")) }
                }
                .onFailure {  err ->
                    state.update { it.copy(dataError = err.message ?: "Phrase doesn't create" ) }
                }
        }
    }

    fun handleCreateDismiss() {
        state.update {
            it.copy(isShowSafeMessage = false)
        }
    }

    fun handleReadyToCreate(walletName: String) {
        state.update {
            it.copy(
                name = walletName.ifEmpty { it.name },
                isShowSafeMessage = true,
            )
        }
    }

    fun handleCreate(onCreated: () -> Unit) {
        state.update { it.copy(isShowSafeMessage = true, loading = true) }
        viewModelScope.launch {
            val newState = withContext(Dispatchers.IO) {
                val phrase = state.value.data.joinToString(" ")
                importWalletOperator(ImportType(WalletType.multicoin), state.value.name, phrase)
            }.fold(
                onSuccess = {
                    onCreated()
                    state.value.copy(loading = false)
                },
                onFailure = { err ->
                    state.value.copy(loading = false, dataError = err.message ?: "Unknown error")
                }
            )
            state.update { newState }
        }
    }
}

data class CreateWalletViewModelState(
    val loading: Boolean = false,
    val error: String = "",
    val generatedNameIndex: Int = 0,
    val name: String = "",
    val nameError: String = "",
    val data: List<String> = emptyList(),
    val dataError: String = "",
    val isShowSafeMessage: Boolean = false,
) {
    fun toUIState() = CreateWalletUIState(
        loading = loading,
        name = name,
        generatedNameIndex = generatedNameIndex,
        nameError = nameError,
        data = data,
        dataError = dataError,
        isShowSafeMessage = isShowSafeMessage,
    )
}

data class CreateWalletUIState(
    val loading: Boolean = false,
    val error: String = "",
    val generatedNameIndex: Int = 0,
    val name: String = "",
    val nameError: String = "",
    val data: List<String> = emptyList(),
    val dataError: String = "",
    val isShowSafeMessage: Boolean = false,
)