package com.gemwallet.android.features.recipient.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.operators.ValidateAddressOperator
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.features.amount.model.AmountParams
import com.gemwallet.android.features.amount.navigation.OnAmount
import com.gemwallet.android.features.recipient.models.RecipientFormError
import com.gemwallet.android.features.recipient.models.RecipientScreenModel
import com.gemwallet.android.features.recipient.models.RecipientScreenState
import com.gemwallet.android.features.recipient.navigation.assetIdArg
import com.gemwallet.android.model.DestinationAddress
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.NameRecord
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

    private val state = MutableStateFlow<RecipientScreenState>(RecipientScreenState.Idle)
    val screenState = state.stateIn(viewModelScope, SharingStarted.Eagerly, RecipientScreenState.Idle)

    private val model = MutableStateFlow(Model())
    val screenModel = model.map { it.toUIModel() }.stateIn(viewModelScope, SharingStarted.Eagerly, RecipientScreenModel())

    val addressState = mutableStateOf("")
    val memoState = mutableStateOf("")
    val nameRecordState = mutableStateOf<NameRecord?>(null)

    fun input() = viewModelScope.launch {
        snapshotFlow { addressState.value }.collectLatest {
            model.update { it.copy(addressError = RecipientFormError.None) }
            nameRecordState.value = null
        }
        snapshotFlow { addressState.value }.collectLatest {
            model.update { it.copy(metaError = RecipientFormError.None) }
        }
    }

    fun scanAddress() {
        state.update { RecipientScreenState.ScanAddress }
    }

    fun scanMemo() {
        state.update { RecipientScreenState.ScanMemo }
    }

    fun scanCancel() {
        state.update { RecipientScreenState.Idle }
    }

    fun setQrData(data: String) {
        val paymentWrapper = uniffi.Gemstone.paymentDecodeUrl(data)
        when (state.value) {
            RecipientScreenState.ScanAddress -> addressState.value = paymentWrapper.address.ifEmpty { data }
            RecipientScreenState.ScanMemo -> memoState.value = paymentWrapper.memo ?: data
            RecipientScreenState.Idle -> {}
        }
        state.update { RecipientScreenState.Idle }
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
            val (address, addressDomain) = if (nameRecord?.name == addressState.value) {
                Pair(nameRecord.address, nameRecord.name)
            } else {
                Pair(addressState.value, "")
            }
            val recipientError = validateRecipient(assetId.chain, address)
            model.update { Model(addressError = recipientError) }
            if (recipientError == RecipientFormError.None) {
                onRecipientComplete(AmountParams.buildTransfer(assetId, DestinationAddress(address, addressDomain), memoState.value))
            }
        }
    }

    data class Model(
        val addressError: RecipientFormError = RecipientFormError.None,
        val metaError: RecipientFormError = RecipientFormError.None,
    ) {

        fun toUIModel(): RecipientScreenModel {
            return RecipientScreenModel(
                addressError = addressError,
                memoError = metaError,
            )
        }
    }
}