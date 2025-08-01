package com.gemwallet.android.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ui.components.clipboard.getPlainText
import com.gemwallet.android.ui.components.designsystem.space4
import com.gemwallet.android.ui.components.fields.TransferTextFieldActions
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16
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

@Composable
fun ColumnScope.AddressChainField(
    chain: Chain?,
    value: String,
    label: String,
    onValueChange: (String, NameRecord?) -> Unit,
    error: String = "",
    editable: Boolean = true,
    searchName: Boolean = true,
    onQrScanner: (() -> Unit)? = null,
) {
    val viewModel: AddressChainViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboard.current.nativeClipboard

    LaunchedEffect(key1 = value) {
        viewModel.onNameRecord(chain, value)
    }

    LaunchedEffect(key1 = uiState.nameRecord?.address) {
        onValueChange(uiState.nameRecord?.name ?: value, uiState.nameRecord)
    }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (it.hasFocus) keyboardController?.show() else keyboardController?.hide()
            },
        value = value,
        singleLine = true,
        readOnly = !editable,
        label = { Text(label) },
        onValueChange = { newValue ->
            if (searchName) {
                viewModel.onInput(newValue, chain)
            }
            onValueChange(newValue, uiState.nameRecord)
        },
        trailingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator16()
                    Spacer(modifier = Modifier.size(8.dp))
                }
                if (uiState.isResolve) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Name is resolved",
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                if (uiState.isFail) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Error,
                        contentDescription = "Name is fail",
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                TransferTextFieldActions(
                    value = value,
                    paste = {
                        onValueChange(
                            clipboardManager.getPlainText() ?: "",
                            uiState.nameRecord
                        )
                    },
                    qrScanner = onQrScanner,
                    onClean = {
                        onValueChange("", null)
                        viewModel.onInput("", null)
                    }
                )
            }
        }
    )
    if (error.isNotEmpty()) {
        Spacer(modifier = Modifier.size(space4))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

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
            try {
                delay(500L)
                val nameRecord = gemClient.resolve(input.lowercase(Locale.getDefault()), chain.string)
                setNameRecord(nameRecord, input)
            } catch (_: Throwable) {}
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