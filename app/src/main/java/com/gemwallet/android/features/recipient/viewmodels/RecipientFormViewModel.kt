package com.gemwallet.android.features.recipient.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.PayloadType
import com.gemwallet.android.blockchain.memo
import com.gemwallet.android.blockchain.operators.ValidateAddressOperator
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.session.SessionRepository
import com.gemwallet.android.features.amount.navigation.OnAmount
import com.gemwallet.android.model.AssetInfo
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.NameRecord
import com.wallet.core.primitives.TransactionType
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
class RecipientFormViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val assetsRepository: AssetsRepository,
    private val validateAddressOperator: ValidateAddressOperator,
) : ViewModel() {

    private val state = MutableStateFlow(RecipientFormState())
    val uiState = state.map { it.toUIState() }.stateIn(viewModelScope, SharingStarted.Eagerly,
        RecipientFormUIState.Loading
    )

    val addressState = mutableStateOf("")
    val memoState = mutableStateOf("")

    fun init(
        assetId: AssetId,
        destinationAddress: String,
        addressDomain: String,
        memo: String
    ) {
        val wallet = sessionRepository.getSession()?.wallet
        if (wallet == null) {
            state.update { it.copy(fatalError = "Select asset") }
            return
        }

        viewModelScope.launch {
            val currentState = state.value.copy()
            val newState = withContext(Dispatchers.IO) { assetsRepository.getById(wallet, assetId) }
                .fold(
                    onSuccess = {
                        addressState.value = destinationAddress.ifEmpty { addressState.value }
                        memoState.value = memo.ifEmpty { memoState.value }
                        currentState.copy(assetInfo = it.first(), addressDomain = addressDomain)
                    }
                ) {
                    currentState.copy(fatalError = it.message ?: "Asset doesn't found")
                }
            state.update { newState }
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

    fun reset() {
        state.update { RecipientFormState() }
    }

    fun onNext(
        input: String,
        nameRecord: NameRecord?,
        memo: String,
        onRecipientComplete: OnAmount
    ) {
        val currentState = state.value
        val asset = currentState.assetInfo?.asset ?: return
        val (address, addressDomain) = if (nameRecord?.name == input) Pair(nameRecord.address, nameRecord.name) else Pair(input, "")
        val recipientError = validateRecipient(asset.id.chain, address)
        state.update {
            currentState.copy(
                addressDomain = addressDomain,
                addressError = recipientError,
            )
        }
        if (recipientError == RecipientFormError.None) {
            onRecipientComplete(
                assetId = asset.id,
                destinationAddress = address,
                addressDomain = addressDomain,
                memo = memo,
                delegationId = "",
                validatorId = "",
                txType = TransactionType.Transfer,
            )
        }
    }
}

data class RecipientFormState(
    val assetInfo: AssetInfo? = null,
//    val address: String = "",
    val addressDomain: String = "",
//    val memo: String = "",
    val scan: ScanType? = null,
    val addressError: RecipientFormError = RecipientFormError.None,
    val metaError: RecipientFormError = RecipientFormError.None,
    val fatalError: String = "",
) {

    fun toUIState(): RecipientFormUIState {
        if (fatalError.isNotEmpty()) {
            return RecipientFormUIState.Fatal(fatalError)
        }

        if (scan != null) {
            return RecipientFormUIState.ScanQr
        }

        return RecipientFormUIState.Idle(
            assetInfo = assetInfo,
            addressDomain = addressDomain,
            hasMemo = (assetInfo?.asset?.id?.chain?.memo() ?: PayloadType.None) != PayloadType.None,
            addressError = addressError,
            memoError = metaError,
        )
    }
}

sealed interface RecipientFormUIState {

    data object Loading : RecipientFormUIState
    class Fatal(val error: String) : RecipientFormUIState

    data class Idle(
        val assetInfo: AssetInfo? = null,
        val addressDomain: String = "",
        val hasMemo: Boolean = false,
        val addressError: RecipientFormError = RecipientFormError.None,
        val memoError: RecipientFormError = RecipientFormError.None,
    ) : RecipientFormUIState

    data object ScanQr : RecipientFormUIState
}

enum class ScanType {
    Address,
    Memo,
}

sealed interface RecipientFormError {
    data object None : RecipientFormError

    data object IncorrectAddress : RecipientFormError
}