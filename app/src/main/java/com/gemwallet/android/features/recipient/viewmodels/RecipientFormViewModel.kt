package com.gemwallet.android.features.recipient.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.operators.ValidateAddressOperator
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.features.amount.navigation.OnAmount
import com.gemwallet.android.features.recipient.models.RecipientFormError
import com.gemwallet.android.features.recipient.models.RecipientScreenModel
import com.gemwallet.android.features.recipient.models.ScanType
import com.gemwallet.android.features.recipient.navigation.assetIdArg
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.NameRecord
import com.wallet.core.primitives.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipientFormViewModel @Inject constructor(
    private val validateAddressOperator: ValidateAddressOperator,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val assetIdStr = savedStateHandle.getStateFlow(assetIdArg, "")
    val assetId = assetIdStr.map { it.toAssetId() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val state = MutableStateFlow(State())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RecipientScreenModel.Idle())

    val addressState = mutableStateOf("")
    val memoState = mutableStateOf("")
    val nameRecordState = mutableStateOf<NameRecord?>(null)

    fun input() = viewModelScope.launch {
        snapshotFlow { addressState.value }.collectLatest {
            state.update { it.copy(addressError = RecipientFormError.None) }
            nameRecordState.value = null
        }
        snapshotFlow { addressState.value }.collectLatest {
            state.update { it.copy(metaError = RecipientFormError.None) }
        }
    }

    fun scanAddress() {
        state.update { it.copy(scan = ScanType.Address) }
    }

    fun scanMemo() {
        state.update { it.copy(scan = ScanType.Memo) }
    }

    fun scanCancel() {
        state.update { it.copy(scan = null) }
    }

    fun setQrData(data: String) {
        val paymentWrapper = uniffi.Gemstone.paymentDecodeUrl(data)
        when (state.value.scan) {
            ScanType.Address -> addressState.value = paymentWrapper.address.ifEmpty { data }
            ScanType.Memo -> memoState.value = paymentWrapper.memo ?: data
            null -> {}
        }
        state.update { it.copy(scan = null) }
    }

    private fun validateRecipient(chain: Chain, recipient: String): RecipientFormError {
        return if (validateAddressOperator(recipient, chain).getOrNull() != true) {
            RecipientFormError.IncorrectAddress
        } else {
            RecipientFormError.None
        }
    }

    fun onNext(onRecipientComplete: OnAmount) {
        viewModelScope.launch {
            val nameRecord = nameRecordState.value
            val assetId = assetId.value ?: return@launch
            val currentState = state.value
            val (address, addressDomain) = if (nameRecord?.name == addressState.value) {
                Pair(nameRecord.address, nameRecord.name)
            } else {
                Pair(addressState.value, "")
            }
            val recipientError = validateRecipient(assetId.chain, address)
            state.update { currentState.copy(addressError = recipientError,) }
            if (recipientError == RecipientFormError.None) {
                onRecipientComplete(
                    assetId = assetId,
                    destinationAddress = address,
                    addressDomain = addressDomain,
                    memo = memoState.value,
                    delegationId = "",
                    validatorId = "",
                    txType = TransactionType.Transfer,
                )
            }
        }
    }

    data class State(
        val scan: ScanType? = null,
        val addressError: RecipientFormError = RecipientFormError.None,
        val metaError: RecipientFormError = RecipientFormError.None,
    ) {

        fun toUIState(): RecipientScreenModel {
            if (scan != null) {
                return RecipientScreenModel.ScanQr
            }

            return RecipientScreenModel.Idle(
                addressError = addressError,
                memoError = metaError,
            )
        }
    }
}