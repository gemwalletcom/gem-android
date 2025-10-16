package com.gemwallet.android.features.import_wallet.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.cases.wallet.ImportError
import com.gemwallet.android.cases.wallet.ImportWalletService
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.ImportType
import com.wallet.core.primitives.NameRecord
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
class ImportViewModel @Inject constructor(
    private val walletsRepository: WalletsRepository,
    private val importWalletService: ImportWalletService,
) : ViewModel() {

    private val state = MutableStateFlow(ImportViewModelState())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ImportUIState())

    fun chainType(walletType: WalletType) {
        state.update {
            it.copy(
                importType = it.importType.copy(walletType = walletType),
                dataError = null
            )
        }
    }

    fun importSelect(importType: ImportType) = viewModelScope.launch {
        val generatedNameIndex = walletsRepository.getNextWalletNumber()
        val chainName = if (importType.walletType == WalletType.multicoin) "" else importType.chain!!.asset().name
        state.update {
            it.copy(
                importType = importType,
                generatedNameIndex = generatedNameIndex,
                chainName = chainName,
                walletName = "",
            )
        }
    }

    fun import(
        name: String,
        generatedName:String,
        data: String,
        nameRecord: NameRecord?,
        onImported: () -> Unit
    ) = viewModelScope.launch {
        state.update { it.copy(loading = true) }

        withContext(Dispatchers.IO) {
            importWalletService.importWallet(
                importType = state.value.importType,
                walletName = name.ifEmpty { generatedName },
                data = if (nameRecord?.address.isNullOrEmpty()) data.trim() else nameRecord.address,
            )
        }.onFailure {  err ->
            state.update { it.copy(dataError = (err as? ImportError) ?: ImportError.CreateError("Unknown error"), loading = false) }
        }.onSuccess {
            state.update { it.copy(dataError = null, loading = false) }
            onImported()
        }
    }
}

data class ImportViewModelState(
    val loading: Boolean = false,
    val error: String = "",
    val importType: ImportType = ImportType(WalletType.multicoin),
    val generatedNameIndex: Int = 0,
    val chainName: String = "",
    val walletName: String = "",
    val walletNameError: String = "",
    val data: String = "",
    val nameRecord: NameRecord? = null,
    val dataError: ImportError? = null,
) {
    fun toUIState(): ImportUIState {
        return ImportUIState(
            loading = loading,
            error = error,
            generatedNameIndex = generatedNameIndex,
            chainName = chainName,
            importType = importType,
            walletName = walletName,
            walletNameError = walletNameError,
            data = data,
            nameRecord = nameRecord,
            dataError = dataError,
        )
    }
}

data class ImportUIState(
    val loading: Boolean = false,
    val error: String = "",
    val importType: ImportType = ImportType(WalletType.multicoin),
    val generatedNameIndex: Int = 0,
    val chainName: String = "",
    val walletName: String = "",
    val walletNameError: String = "",
    val data: String = "",
    val nameRecord: NameRecord? = null,
    val dataError: ImportError? = null,
    val isShowSafeMessage: Boolean = false,
)